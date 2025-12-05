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
import java.util.List;
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
        logger.info("Login attempt for email: {}", loginRequest.getEmail());
        
        // Get customer details by email
        CustomerDto customer = null;
        try {
            // Primero intentar con el endpoint directo (si existe)
            try {
                ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByEmail(loginRequest.getEmail());
                customer = response.getBody();
                logger.info("Customer found via direct endpoint: {}", customer != null ? customer.getEmail() : "null");
            } catch (FeignException e) {
                // Si falla, obtener todos y filtrar
                logger.warn("Direct endpoint failed, fetching all customers and filtering");
                ResponseEntity<List<CustomerDto>> allCustomersResponse = customersBsFeignClient.selectAllCustomer();
                List<CustomerDto> allCustomers = allCustomersResponse.getBody();
                if (allCustomers != null) {
                    customer = allCustomers.stream()
                        .filter(c -> c.getEmail().equalsIgnoreCase(loginRequest.getEmail()))
                        .findFirst()
                        .orElse(null);
                }
            }
        } catch (FeignException e) {
            logger.error("Error fetching customer: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (customer == null) {
            logger.warn("Customer not found for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.debug("Stored password hash: {}", customer.getPassword());
        logger.debug("Provided password: {}", loginRequest.getPassword());
        
        // Verify password
        // Intentar primero con BCrypt (contraseñas nuevas)
        boolean passwordMatches = false;
        try {
            passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword());
            logger.info("BCrypt password match: {}", passwordMatches);
        } catch (Exception e) {
            // Si falla BCrypt, puede ser que la contraseña esté en texto plano (usuarios antiguos)
            logger.warn("BCrypt failed, checking plain text password for legacy user: {}", e.getMessage());
        }
        
        // Si BCrypt no funcionó, intentar comparación directa (usuarios legacy)
        if (!passwordMatches) {
            passwordMatches = loginRequest.getPassword().equals(customer.getPassword());
            logger.info("Plain text password match: {}", passwordMatches);
        }
        
        if (!passwordMatches) {
            logger.warn("Password verification failed for email: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.info("Login successful for email: {}", loginRequest.getEmail());
        
        // Get roles, default to USER if not set
        Set<String> roles = customer.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("USER");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                customer.getEmail(), 
                roles, 
                customer.getIdCustomer()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getEmail());

        // Log para debugging
        logger.info("Building AuthResponse - name: {}, lastName: {}, idCustomer: {}", 
            customer.getName(), 
            customer.getLastName(), 
            customer.getIdCustomer());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .email(customer.getEmail())
                .roles(roles)
                .name(customer.getName())
                .lastName(customer.getLastName())
                .userId(customer.getIdCustomer())
                .build();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Create customer DTO
        CustomerDto customerDto = new CustomerDto();
        customerDto.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        customerDto.setName(registerRequest.getName());
        customerDto.setLastName(registerRequest.getLastName());
        customerDto.setEmail(registerRequest.getEmail());
        customerDto.setBirthDate(registerRequest.getBirthDate());
        customerDto.setAge(registerRequest.getAge());
        customerDto.setPromoCode(registerRequest.getPromoCode());
        
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
                throw new IllegalArgumentException("Email already exists");
            }
            throw new IllegalArgumentException("Failed to register user: " + e.contentUTF8());
        }

        // Get the created customer to get the ID
        CustomerDto createdCustomer = null;
        try {
            // Primero intentar con el endpoint directo
            try {
                ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByEmail(registerRequest.getEmail());
                createdCustomer = response.getBody();
            } catch (FeignException e) {
                // Si falla, obtener todos y filtrar
                logger.warn("Direct endpoint failed, fetching all customers and filtering");
                ResponseEntity<List<CustomerDto>> allCustomersResponse = customersBsFeignClient.selectAllCustomer();
                List<CustomerDto> allCustomers = allCustomersResponse.getBody();
                if (allCustomers != null) {
                    createdCustomer = allCustomers.stream()
                        .filter(c -> c.getEmail().equalsIgnoreCase(registerRequest.getEmail()))
                        .findFirst()
                        .orElse(null);
                }
            }
        } catch (FeignException e) {
            logger.error("Error fetching created customer: {}", e.getMessage());
            throw new IllegalArgumentException("User registered but failed to fetch details");
        }

        Long customerId = createdCustomer != null ? createdCustomer.getIdCustomer() : null;

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                registerRequest.getEmail(), 
                roles, 
                customerId
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(registerRequest.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .email(registerRequest.getEmail())
                .roles(roles)
                .name(createdCustomer != null ? createdCustomer.getName() : registerRequest.getName())
                .lastName(createdCustomer != null ? createdCustomer.getLastName() : registerRequest.getLastName())
                .userId(customerId)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Get email from refresh token
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // Get customer details to get roles and ID
        CustomerDto customer = null;
        try {
            // Primero intentar con el endpoint directo
            try {
                ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByEmail(email);
                customer = response.getBody();
            } catch (FeignException e) {
                // Si falla, obtener todos y filtrar
                logger.warn("Direct endpoint failed, fetching all customers and filtering");
                ResponseEntity<List<CustomerDto>> allCustomersResponse = customersBsFeignClient.selectAllCustomer();
                List<CustomerDto> allCustomers = allCustomersResponse.getBody();
                if (allCustomers != null) {
                    customer = allCustomers.stream()
                        .filter(c -> c.getEmail().equalsIgnoreCase(email))
                        .findFirst()
                        .orElse(null);
                }
            }
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
                email, 
                roles, 
                customer.getIdCustomer()
        );

        // Generate new refresh token
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .email(email)
                .roles(roles)
                .name(customer.getName())
                .lastName(customer.getLastName())
                .userId(customer.getIdCustomer())
                .build();
    }
}