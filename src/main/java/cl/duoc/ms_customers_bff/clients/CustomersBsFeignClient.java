package cl.duoc.ms_customers_bff.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ms-customers-bs", url = "http://localhost:8081")
public interface CustomersBsFeignClient {

}
