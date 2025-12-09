package ch.frupp.lecturevault.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(UserController.RegistrationRequest request) {
        String username = request.username();
        String password = request.password();

        // Check if username exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        // Password length validation (example)
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }

        // Create user entity and hash password
        User user = new User();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // TODO: Hash password client-side (duh!)
        user.setRole(request.role() == null ? "USER" : request.role().toUpperCase());

        logger.info("Registering user {}", username);

        // Save user and return entity
        return userRepository.save(user);
    }
}
