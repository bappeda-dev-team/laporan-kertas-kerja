package cc.kertaskerja.laporan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
              // Disable CSRF since this is typically handled at the frontend level (especially for APIs)
              .csrf(AbstractHttpConfigurer::disable)

              // Enable CORS with the configuration defined below
              .cors(Customizer.withDefaults())

              // Authorization rules
              .authorizeHttpRequests(authz -> authz
                    // Allow preflight requests
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Allow access to Swagger/OpenAPI docs
                    .requestMatchers(
                          "/swagger-ui/**",
                          "/swagger-ui.html",
                          "/v3/api-docs/**",
                          "/swagger-resources/**",
                          "/webjars/**"
                    ).permitAll()

                    // Public APIs (optional)
                    .requestMatchers(
                          "/api/public/**",
                          "/actuator/**",
                          "/api/external/**"
                    ).permitAll()

                    // Allow upload endpoint if it needs to be accessed without authentication
                    .requestMatchers(HttpMethod.POST, "/kta/api/pengajuan/upload/save").permitAll()

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
              )

              // Basic auth (replace with JWT if you have it)
              .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins — add your frontend URLs here
        configuration.setAllowedOrigins(List.of(
              "http://localhost:3000",
              "https://manrisk.kertaskerja.cc"
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(List.of(
              "GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies / authorization headers)
        configuration.setAllowCredentials(true);

        // Apply to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var user = User.builder()
              .username("admin")
              .password(passwordEncoder.encode("admin123"))
              .roles("ADMIN")
              .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
