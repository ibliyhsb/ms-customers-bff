package cl.duoc.ms_customers_bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"jwt.secret=test-secret-key-for-testing-purposes-make-it-longer-than-256-bits",
	"jwt.expiration=900000",
	"jwt.refresh-expiration=604800000",
	"cors.allowed-origins=http://localhost:3000"
})
class MsCustomersBffApplicationTests {

	@Test
	void contextLoads() {
	}

}
