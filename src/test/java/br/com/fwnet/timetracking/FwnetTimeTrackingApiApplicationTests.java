package br.com.fwnet.timetracking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=fwnet-test-jwt-secret-only-for-automated-tests-1234567890",
		"jwt.expiration-ms=3600000"
})
class FwnetTimeTrackingApiApplicationTests {

	@Test
	void contextLoads() {
	}
}