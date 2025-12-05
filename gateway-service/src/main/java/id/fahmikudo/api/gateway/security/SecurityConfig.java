package id.fahmikudo.api.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityErrorResponder errorResponder;

    public SecurityConfig(SecurityErrorResponder errorResponder) {
        this.errorResponder = errorResponder;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**", "/auth/**").permitAll()
                        .requestMatchers("/api/products/**").hasAuthority("SCOPE_product.read")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                errorResponder.write(request, response, org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Authentication required",
                        authException.getMessage());
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                errorResponder.write(request, response, org.springframework.http.HttpStatus.FORBIDDEN,
                        "Access denied",
                        accessDeniedException.getMessage());
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }
}
