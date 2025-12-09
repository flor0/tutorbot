package ch.frupp.lecturevault.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new UserAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Expose the SecurityContextRepository as a bean so other components (controllers/filters)
    // can use the exact same implementation (important when using Spring Session, etc.)
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider,
                                           SecurityContextRepository securityContextRepository) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth ->
                auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
        );

        // Use the injected repository
        http.securityContext(ctx -> ctx.securityContextRepository(securityContextRepository));

        http.sessionManagement(session -> {
                session.maximumSessions(1).maxSessionsPreventsLogin(true);
                session.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession);
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            }
        );

        http.logout(session -> {
           session.logoutUrl("/api/auth/logout");
           session.addLogoutHandler(new HeaderWriterLogoutHandler(
                   new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES)
           ));
           session.deleteCookies("JSESSIONID");
        });

        http.authenticationProvider(authenticationProvider);

        return http.build();
    }
}
