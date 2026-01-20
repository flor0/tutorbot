package ch.frupp.tutorbot.user;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class UserController {

//    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    // Property to enable/disable registration. Default true so tests that construct the controller manually still pass.
    @Value("${app.allow-registration:true}")
    private boolean allowRegistration = false;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse) {
        try {
            // 1) Build an Authentication token from username/password
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.username(), request.password());

            // 2) Tell Spring Security to authenticate (this calls your AuthenticationProvider)
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Persist the authentication into the SecurityContext and into the HTTP session
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            // also set it on the SecurityContextHolder (thread-bound)
            SecurityContextHolder.setContext(context);

            // Use the configured SecurityContextRepository to save the context (this will write to the session
            // and also perform any additional actions the repository requires)
            securityContextRepository.saveContext(context, servletRequest, servletResponse);

            // 3) If we reach here, authentication succeeded.
            User principal = (User) authentication.getPrincipal();
            log.info("Login from username: {} with authorities: {}",
                    principal.getUsername(), authentication.getAuthorities());


            // 4) Return something simple to the frontend
            return ResponseEntity.ok(new LoginResponse(
                    authentication.getName(),   // username
                    authentication.getAuthorities().toString()
            ));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    // Debug helper: return current authentication info (call this after login to verify session)
    @GetMapping("/whoami")
    public ResponseEntity<?> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no authentication");
        return ResponseEntity.ok(new LoginResponse(auth.getName(), auth.getAuthorities().toString()));
    }

    // Logout helper: clear security context and invalidate session
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().body("logged out");
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String username, String authorities) {}

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(HttpStatus.OK).body("Placeholder test");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        if (!allowRegistration) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Registration is disabled");
        }
        try {
            User savedUser = userService.register(registrationRequest);
            return ResponseEntity.ok().body(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public record RegistrationRequest(
            String username,
            String email,
            String password,
            String role
    ) {}
}
