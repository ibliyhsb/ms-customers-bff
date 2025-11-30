package cl.duoc.ms_customers_bff.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class CustomerDto {

    @JsonProperty(value = "id_customer")
     private Long idCustomer;

    @JsonProperty(value = "username")
     private String username;

    @JsonProperty(value = "password")
     private String password;

    @JsonProperty(value = "name")
     private String name;

    @JsonProperty(value = "last_name")
     private String lastName;

    @JsonProperty(value = "email")
     private String email;

    @JsonProperty(value = "roles")
     private Set<String> roles;

}
