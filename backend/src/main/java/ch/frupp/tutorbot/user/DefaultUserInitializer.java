package ch.frupp.tutorbot.user;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-user.username:florian}")
    private String defaultUsername;

    @Value("${app.default-user.password:verystrongpasswordforproduction}")
    private String defaultPassword;

    @Value("${app.default-user.role:ADMIN}")
    private String defaultRole;

    public DefaultUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void ensureDefaultUser() {
        userRepository.findByUsername(defaultUsername).ifPresentOrElse(u -> {
            logger.info("Default user '{}' already exists, skipping creation", defaultUsername);
        }, () -> {
            User u = new User();
            u.setUsername(defaultUsername);
            u.setPassword(passwordEncoder.encode(defaultPassword));
            u.setRole(defaultRole);
            u.setEnabled(true);
            userRepository.save(u);
            logger.info("Created default user '{}' with role {}", defaultUsername, defaultRole);
        });
    }
}
