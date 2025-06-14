package cl.duoc.ms_customers_bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsCustomersBffApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCustomersBffApplication.class, args);
	}

}
