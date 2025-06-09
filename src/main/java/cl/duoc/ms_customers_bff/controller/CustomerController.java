package cl.duoc.ms_customers_bff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_customers_bff.model.dto.CustomerDto;
import cl.duoc.ms_customers_bff.service.CustomerService;
import feign.FeignException.FeignClientException;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    CustomerService customerService;

     @GetMapping("/GetCustomerById/{idCustomer}")
    public ResponseEntity<?> getCustomerById(@PathVariable("idCustomer") Long idCustomer){
    return customerService.getCustomerById(idCustomer);

}

    @GetMapping()
    public ResponseEntity<List<CustomerDto>> selectAllCustomers(){
        ResponseEntity<List<CustomerDto>> listaCustomerDto = customerService.selectAllCustomer();

        return listaCustomerDto;
    }

    @GetMapping("/authenticate/{username}/{password}")
    public ResponseEntity<String> authenticateCustomer(@PathVariable("username") String username, @PathVariable("password") String password){
        return customerService.authenticateCustomer(username, password);
    }

    @PostMapping()
    public ResponseEntity<String> insertCustomer(@RequestBody CustomerDto customerDto){
        try{
        return customerService.insertCustomer(customerDto);}
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());
        }
    }

    @DeleteMapping("/DeleteCustomerById/{idCustomer}")
    public ResponseEntity<String> deleteCustomer(@PathVariable("idCustomer") Long idCustomer){
        try{
        return customerService.deleteCustomer(idCustomer);}
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());

        }
    }

    @PutMapping("/UpdateCustomer")
    public ResponseEntity<String> updateCustomer(@RequestBody CustomerDto customerDto){
        try{
        return customerService.updateCustomer(customerDto);}
        catch(FeignClientException feignClientException){
            return ResponseEntity.status(feignClientException.status()).body(feignClientException.contentUTF8());
        }
    }
}
