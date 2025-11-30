package cl.duoc.ms_customers_bff.service;

import cl.duoc.ms_customers_bff.clients.CustomersBsFeignClient;
import cl.duoc.ms_customers_bff.model.dto.AuthResponse;
import cl.duoc.ms_customers_bff.model.dto.CustomerDto;
import cl.duoc.ms_customers_bff.model.dto.LoginRequest;
import cl.duoc.ms_customers_bff.model.dto.RefreshTokenRequest;
import cl.duoc.ms_customers_bff.model.dto.RegisterRequest;
import cl.duoc.ms_customers_bff.security.JwtTokenProvider;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final CustomersBsFeignClient customersBsFeignClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(CustomersBsFeignClient customersBsFeignClient, 
                       JwtTokenProvider jwtTokenProvider, 
                       PasswordEncoder passwordEncoder) {
        this.customersBsFeignClient = customersBsFeignClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        // Authenticate against ms-customers-bs
        boolean isAuthenticated = customersBsFeignClient.authenticateCustomer(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
        );

        if (!isAuthenticated) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Get customer details including roles
        CustomerDto customer;
        try {
            ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByUsername(loginRequest.getUsername());
            customer = response.getBody();
        } catch (FeignException e) {
            logger.error("Error fetching customer by username: {}", e.getMessage());
            throw new IllegalArgumentException("Error fetching user details");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        // Get roles, default to USER if not set
        Set<String> roles = customer.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("USER");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getUsername(), 
                roles, 
                customer.getIdCustomer()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .username(customer.getUsername())
                .roles(roles)
                .build();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Create customer DTO
        CustomerDto customerDto = new CustomerDto();
        customerDto.setUsername(registerRequest.getUsername());
        customerDto.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        customerDto.setName(registerRequest.getName());
        customerDto.setLastName(registerRequest.getLastName());
        customerDto.setEmail(registerRequest.getEmail());
        
        // Set default role as USER
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        customerDto.setRoles(roles);

        // Save to ms-customers-bs
        try {
            ResponseEntity<String> response = customersBsFeignClient.insertCustomer(customerDto);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Failed to register user");
            }
        } catch (FeignException e) {
            logger.error("Error registering customer: {}", e.getMessage());
            if (e.status() == 409) {
                throw new IllegalArgumentException("Username or email already exists");
            }
            throw new IllegalArgumentException("Failed to register user: " + e.contentUTF8());
        }

        // Get the created customer to get the ID
        CustomerDto createdCustomer;
        try {
            ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByUsername(registerRequest.getUsername());
            createdCustomer = response.getBody();
        } catch (FeignException e) {
            logger.error("Error fetching created customer: {}", e.getMessage());
            throw new IllegalArgumentException("User registered but failed to fetch details");
        }

        Long customerId = createdCustomer != null ? createdCustomer.getIdCustomer() : null;

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                registerRequest.getUsername(), 
                roles, 
                customerId
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(registerRequest.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .username(registerRequest.getUsername())
                .roles(roles)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Get username from refresh token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Get customer details to get roles and ID
        CustomerDto customer;
        try {
            ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByUsername(username);
            customer = response.getBody();
        } catch (FeignException e) {
            logger.error("Error fetching customer for token refresh: {}", e.getMessage());
            throw new IllegalArgumentException("Error refreshing token");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        // Get roles
        Set<String> roles = customer.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("USER");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                username, 
                roles, 
                customer.getIdCustomer()
        );

        // Generate new refresh token
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .username(username)
                .roles(roles)
                .build();
    }
}
