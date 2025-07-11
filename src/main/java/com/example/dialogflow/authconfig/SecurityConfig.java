package com.example.dialogflow.authconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import this!
import org.springframework.security.config.Customizer; // Import this!
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays; // Import for Arrays.asList

@Configuration
public class SecurityConfig {

    // Define your CORS configuration as a Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Crucial: Add the origins your frontend runs on
        // "http://localhost" because your JS is sending this origin
        config.addAllowedOrigin("http://localhost");
        // "http://192.168.0.105" if your Yii2 app might be accessed by its IP
        // (Note: Removed :80/hrms as /hrms is a path, not part of the origin.
        // If your Yii2 app truly runs on 192.168.0.105:80, then that's the origin.)
        // Ensure you have the correct IP/Port for your Yii2 machine if not localhost
        // If your Yii2 app is hosted at 192.168.0.105:80, add that.
        // Assuming your previous IP was a typo and 192.168.0.105 is the correct one now.
        config.addAllowedOrigin("http://192.168.0.105");


        // If your Yii2 app could be accessed from a different specific port on localhost:
        // config.addAllowedOrigin("http://localhost:8000"); // e.g., if Yii2 dev server runs on 8000

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Explicitly allow OPTIONS
        config.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        config.setAllowCredentials(true); // Allow credentials (cookies, auth headers)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all paths
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Typically disabled for stateless APIs if not using CSRF tokens
                .cors(Customizer.withDefaults()) // Enable CORS using the Bean defined above

                .authorizeHttpRequests(authorize -> authorize
                        // CRITICAL: Allow OPTIONS requests to pass through without authentication checks.
                        // This prevents redirects for preflight requests.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Allow your API chat endpoint to be accessed without authentication.
                        // If you *do* need authentication for /api/chat, then your Yii2 app
                        // must send valid credentials (e.g., Authorization header).
                        .requestMatchers("/api/chat/**").permitAll() // Matches /api/chat and /api/chat/

                        // Configure other security rules for other endpoints as needed
                        .anyRequest().authenticated() // All other requests require authentication
                )
                // You might want to disable formLogin or httpBasic if you're using token-based auth
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable); // Disable if you're not using basic auth

        return http.build();
    }
}