package com.example.tendawaks.authconfig; // Adjust package if different

import com.example.tendawaks.service.JWTService; // Your JWT Service
import com.example.tendawaks.service.UsersService; // Your UserDetailsService (or UsersService if it implements UserDetailsService)
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component // If you're using @Autowired in SecurityConfig's constructor for jwtFilter, you might not need @Component here, or vice-versa.
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService usersService; // Or UserDetailsService, depending on your actual implementation

    // Define your public paths explicitly here.
    // This list MUST match the paths in your SecurityConfig's .requestMatchers().permitAll()
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/chat", // The endpoint for your chat
            "/api/auth/", // Matches /api/auth/**
            "/oauth2/",   // Matches /oauth2/**
            "/login/",    // Matches /login/**
            "/api/token/refresh",
            "/api/forgot-password",
            "/api/reset-password",
            "/v3/api-docs/", // Matches /v3/api-docs/**
            "/swagger-ui/",  // Matches /swagger-ui/**
            "/swagger-ui.html" // Exact match for swagger-ui.html
    );

    // Constructor to inject services
    public JWTFilter(JWTService jwtService, UserDetailsService usersService) {
        this.jwtService = jwtService;
        this.usersService = usersService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI(); // Get the incoming request URI

        // --- Start of the NEW bypass logic ---
        // Check if the current request path is one of the publicly accessible paths
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/")) {
                // For paths like "/api/auth/" which should match "/api/auth/login", "/api/auth/register", etc.
                return path.startsWith(publicPath);
            }
            // For exact matches like "/api/chat" or "/swagger-ui.html"
            return path.equals(publicPath);
        });

        if (isPublicPath) {
            // If it's a public path, skip JWT validation entirely and proceed to the next filter in the chain.
            filterChain.doFilter(request, response);
            return; // Important: Exit the filter method here.
        }
        // --- End of the NEW bypass logic ---


        // --- Existing JWT validation logic (only for non-public paths) ---
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Extract the token after "Bearer "
            try {
                username = jwtService.extractUserName(jwt);
            } catch (Exception e) { // Catch specific JWT parsing/validation exceptions if your service throws them
                logger.warn("Error extracting username from JWT: {}", e.getMessage());
                // You might want to log this or return an error response for invalid tokens on non-public paths
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Or SC_BAD_REQUEST if token malformed
                response.getWriter().write("Invalid JWT Token");
                return;
            }
        } else {
            // This block will now only be hit for NON-PUBLIC paths if no Authorization header is found
            logger.warn("Missing or invalid Authorization header for non-public path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Or SC_BAD_REQUEST if this is the desired response
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }


        // If username is valid and no authentication is currently set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.usersService.loadUserByUsername(username);

            if (jwtService.isValidToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT Token expired or invalid");
                return;
            }
        }

        // Proceed to the next filter in the chain (or the DispatcherServlet if no more filters)
        filterChain.doFilter(request, response);
    }

    // You might need a logger if not already present
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JWTFilter.class);
}