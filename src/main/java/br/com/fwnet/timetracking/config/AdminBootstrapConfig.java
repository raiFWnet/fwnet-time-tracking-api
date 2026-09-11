package br.com.fwnet.timetracking.config;

import br.com.fwnet.timetracking.entity.User;
import br.com.fwnet.timetracking.enums.Role;
import br.com.fwnet.timetracking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Configuration
@Profile("dev")
@ConditionalOnProperty(
        name = "bootstrap.admin.enabled",
        havingValue = "true"
)
public class AdminBootstrapConfig {

    @Bean
    public ApplicationRunner adminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${bootstrap.admin.full-name}") String fullName,
            @Value("${bootstrap.admin.email}") String email,
            @Value("${bootstrap.admin.password}") String password
    ) {
        return args -> {

            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                        "BOOTSTRAP_ADMIN_EMAIL e BOOTSTRAP_ADMIN_PASSWORD devem ser informados."
                );
            }

            if (userRepository.existsByEmail(email)) {
                return;
            }

            OffsetDateTime now = OffsetDateTime.now();

            User admin = new User();
            admin.setId(UUID.randomUUID());
            admin.setFullName(fullName);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(now);
            admin.setUpdatedAt(now);

            userRepository.save(admin);
        };
    }
}