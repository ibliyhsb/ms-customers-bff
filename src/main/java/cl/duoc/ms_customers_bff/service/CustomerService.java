package cl.duoc.ms_customers_bff.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.duoc.ms_customers_bff.clients.CustomersBsFeignClient;
import cl.duoc.ms_customers_bff.model.dto.CustomerDto;
import feign.FeignException;

@Service
public class CustomerService {

    @Autowired
    CustomersBsFeignClient customersBsFeignClient;

public ResponseEntity<?> getCustomerById(Long idCustomer) {
    try {
        return customersBsFeignClient.getCustomerById(idCustomer);
    } catch (FeignException feignException) {
        return ResponseEntity.status(feignException.status()).body(feignException.contentUTF8());
    }
}

public ResponseEntity<List<CustomerDto>> selectAllCustomer(){
    List<CustomerDto> listaCustomerDto = customersBsFeignClient.selectAllCustomer().getBody();
    
    return (listaCustomerDto != null)? new ResponseEntity<>(listaCustomerDto, HttpStatus.OK):
                                       new ResponseEntity<>(HttpStatus.NOT_FOUND);
}


public ResponseEntity<String> authenticateCustomer(String username, String password){  

    boolean authenticate = customersBsFeignClient.authenticateCustomer(username, password);

    if (authenticate == false){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("The username and password do not match.");
    }

    else {
        return ResponseEntity.status(HttpStatus.OK).body("Session successfully started.");
    }
}


public ResponseEntity<String> insertCustomer(CustomerDto customerDto){
    return customersBsFeignClient.insertCustomer(customerDto);
}


public ResponseEntity<String> deleteCustomer(Long idCustomer){
    return customersBsFeignClient.deleteCustomer(idCustomer);
}


public ResponseEntity<String> updateCustomer(CustomerDto customerDto){
    return customersBsFeignClient.updateCustomer(customerDto);
}

public ResponseEntity<?> getCurrentUserProfile(String username) {
    try {
        ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByUsername(username);
        if (response.getBody() != null) {
            CustomerDto customer = response.getBody();
            // Don't return password in profile
            customer.setPassword(null);
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    } catch (FeignException feignException) {
        return ResponseEntity.status(feignException.status()).body(feignException.contentUTF8());
    }
}

public ResponseEntity<String> updateCurrentUserProfile(String username, CustomerDto customerDto) {
    try {
        // First get the current user to get their ID
        ResponseEntity<CustomerDto> response = customersBsFeignClient.getCustomerByUsername(username);
        if (response.getBody() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        CustomerDto existingCustomer = response.getBody();
        
        // Only allow updating certain fields for the user's own profile
        existingCustomer.setName(customerDto.getName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setEmail(customerDto.getEmail());
        // Don't allow users to change their own username or roles
        
        return customersBsFeignClient.updateCustomer(existingCustomer);
    } catch (FeignException feignException) {
        return ResponseEntity.status(feignException.status()).body(feignException.contentUTF8());
    }
}
}
