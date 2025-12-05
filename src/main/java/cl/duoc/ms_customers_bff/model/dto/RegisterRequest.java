package cl.duoc.ms_customers_bff.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "Name is required")
    @JsonProperty("nombre")
    private String name;

    @NotBlank(message = "Last name is required")
    @JsonProperty("apellidos")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @JsonProperty("email")
    private String email;

    @JsonProperty("fechaNacimiento")
    private LocalDate birthDate;

    @JsonProperty("edad")
    private Integer age;

    @JsonProperty("codigoPromo")
    private String promoCode;
}
