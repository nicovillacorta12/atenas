package ar.edu.utn.frc.atenas.seguimiento.config;

import ar.edu.utn.frc.atenas.commons.seguridad.ConversorJwtKeycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ConversorJwtKeycloak conversorJwtKeycloak;

    public SecurityConfig(ConversorJwtKeycloak conversorJwtKeycloak) {
        this.conversorJwtKeycloak = conversorJwtKeycloak;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/api/seguimiento/v3/api-docs",
                                "/api/seguimiento/v3/api-docs/**",
                                "/api/seguimiento/swagger-ui.html",
                                "/api/seguimiento/swagger-ui/**",
                                "/webjars/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorJwtKeycloak)));

        return http.build();
    }
}
