package ch.frupp.tutorbot.auth.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DemoReadOnlyFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean isDemoUser = authentication != null
				&& authentication.isAuthenticated()
				&& authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_DEMO".equals(authority.getAuthority()));

		boolean isMutatingRequest = switch (request.getMethod()) {
			case "POST", "PUT", "PATCH", "DELETE" -> true;
			default -> false;
		};

		boolean isLogoutRequest = "/api/auth/logout".equals(request.getRequestURI());

		if (isDemoUser && isMutatingRequest && !isLogoutRequest) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Demo users are read-only");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
