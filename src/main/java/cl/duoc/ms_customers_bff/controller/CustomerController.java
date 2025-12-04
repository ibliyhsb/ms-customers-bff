package cl.duoc.ms_customers_bff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_customers_bff.model.dto.CustomerDto;
import cl.duoc.ms_customers_bff.security.CustomUserDetails;
import cl.duoc.ms_customers_bff.service.CustomerService;
import feign.FeignException.FeignClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return customerService.getCurrentUserProfile(userDetails.getEmail());
        }
        return ResponseEntity.status(401).body("User not authenticated");
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update current user profile", description = "Update the profile of the currently authenticated user")
    public ResponseEntity<String> updateCurrentUserProfile(@RequestBody CustomerDto customerDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return customerService.updateCurrentUserProfile(userDetails.getEmail(), customerDto);
        }
        return ResponseEntity.status(401).body("User not authenticated");
    }

    @GetMapping("/GetCustomerById/{idCustomer}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get customer by ID", description = "Get a customer by their ID (Admin only)")
    public ResponseEntity<?> getCustomerById(@PathVariable("idCustomer") Long idCustomer){
        return customerService.getCustomerById(idCustomer);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers", description = "Get a list of all customers (Admin only)")
    public ResponseEntity<List<CustomerDto>> selectAllCustomers(){
        ResponseEntity<List<CustomerDto>> listaCustomerDto = customerService.selectAllCustomer();
        return listaCustomerDto;
    }

    /**
     * @deprecated This endpoint is deprecated. Use /api/auth/login for JWT-based authentication.
     */
    @Deprecated
    @PostMapping("/authenticate/{email}/{password}")
    @Operation(summary = "Authenticate customer (Deprecated)", description = "This endpoint is deprecated. Use /api/auth/login instead.")
    public ResponseEntity<String> authenticateCustomer(@PathVariable("email") String email, @PathVariable("password") String password){
        return customerService.authenticateCustomer(email, password);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create customer", description = "Create a new customer (Admin only)")
    public ResponseEntity<String> insertCustomer(@RequestBody CustomerDto customerDto){
        try{
            return customerService.insertCustomer(customerDto);
        }
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());
        }
    }

    @DeleteMapping("/DeleteCustomerById/{idCustomer}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer", description = "Delete a customer by ID (Admin only)")
    public ResponseEntity<String> deleteCustomer(@PathVariable("idCustomer") Long idCustomer){
        try{
            return customerService.deleteCustomer(idCustomer);
        }
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());
        }
    }

    @PutMapping("/UpdateCustomer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer", description = "Update any customer (Admin only)")
    public ResponseEntity<String> updateCustomer(@RequestBody CustomerDto customerDto){
        try{
            return customerService.updateCustomer(customerDto);
        }
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());
        }
    }
}
