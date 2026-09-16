package br.com.fwnet.timetracking;

import br.com.fwnet.timetracking.repository.TimeRecordCorrectionRepository;
import br.com.fwnet.timetracking.repository.TimeRecordRepository;
import br.com.fwnet.timetracking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"jwt.secret=fwnet-test-jwt-secret-only-for-automated-tests-1234567890",
		"jwt.expiration-ms=3600000",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
				+ "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
				+ "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class FwnetTimeTrackingApiApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private TimeRecordRepository timeRecordRepository;

	@MockitoBean
	private TimeRecordCorrectionRepository timeRecordCorrectionRepository;

	@Test
	void contextLoads() {
	}
}