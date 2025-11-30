package cl.duoc.ms_customers_bff.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    private String password;
}
