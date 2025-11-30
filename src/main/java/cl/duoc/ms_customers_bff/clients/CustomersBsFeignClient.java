package cl.duoc.ms_customers_bff.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import cl.duoc.ms_customers_bff.model.dto.CustomerDto;

@FeignClient(name = "ms-customers-bs", url = "http://localhost:8081")
public interface CustomersBsFeignClient {

    
    @GetMapping("/api/customers/authenticate/{username}/{password}")
    public boolean authenticateCustomer (@PathVariable("username") String username, @PathVariable("password") String password);

    @GetMapping("/api/customers")
    public ResponseEntity<List<CustomerDto>> selectAllCustomer();

    @GetMapping("/api/customers/GetCustomerById/{idCustomer}")
    public ResponseEntity<?> getCustomerById(@PathVariable("idCustomer") Long idCustomer);

    @GetMapping("/api/customers/GetCustomerByUsername/{username}")
    public ResponseEntity<CustomerDto> getCustomerByUsername(@PathVariable("username") String username);

    @PostMapping("/api/customers")
    public ResponseEntity<String> insertCustomer(@RequestBody CustomerDto customerDto);

    @DeleteMapping("/api/customers/DeleteCustomerById/{idCustomer}")
    public ResponseEntity<String> deleteCustomer(@PathVariable("idCustomer") Long idCustomer);
    
    
    @PutMapping("/api/customers/UpdateCustomer")
    public ResponseEntity<String> updateCustomer(@RequestBody CustomerDto customerDto);

    // Methods with Authorization header for inter-service communication
    @GetMapping("/api/customers")
    public ResponseEntity<List<CustomerDto>> selectAllCustomer(@RequestHeader("Authorization") String token);

    @GetMapping("/api/customers/GetCustomerById/{idCustomer}")
    public ResponseEntity<?> getCustomerById(@PathVariable("idCustomer") Long idCustomer, @RequestHeader("Authorization") String token);

    @GetMapping("/api/customers/GetCustomerByUsername/{username}")
    public ResponseEntity<CustomerDto> getCustomerByUsername(@PathVariable("username") String username, @RequestHeader("Authorization") String token);
}
