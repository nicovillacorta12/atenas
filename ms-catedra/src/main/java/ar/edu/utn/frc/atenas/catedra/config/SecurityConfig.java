package ar.edu.utn.frc.atenas.catedra.config;

import ar.edu.utn.frc.atenas.commons.seguridad.ConversorJwtKeycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static ar.edu.utn.frc.atenas.commons.seguridad.Roles.DOCENTE;
import static ar.edu.utn.frc.atenas.commons.seguridad.Roles.JEFE_CATEDRA;

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
                                "/api/catedra/v3/api-docs",
                                "/api/catedra/v3/api-docs/**",
                                "/api/catedra/swagger-ui.html",
                                "/api/catedra/swagger-ui/**",
                                "/webjars/**")
                        .permitAll()

                        // Estructura académica: solo el jefe la administra.
                        .requestMatchers(HttpMethod.POST, "/api/catedra/cursos-lectivos").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.GET, "/api/catedra/cursos-lectivos").hasAnyRole(JEFE_CATEDRA, DOCENTE)
                        .requestMatchers(HttpMethod.POST, "/api/catedra/comisiones").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.GET, "/api/catedra/comisiones").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.POST, "/api/catedra/cursos-lectivos/*/comisiones-lectivas")
                        .hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.GET, "/api/catedra/cursos-lectivos/*/comisiones-lectivas")
                        .hasAnyRole(JEFE_CATEDRA, DOCENTE)

                        // Docentes y asignaciones: administración exclusiva del jefe.
                        .requestMatchers(HttpMethod.POST, "/api/catedra/docentes").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.GET, "/api/catedra/docentes").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.PATCH, "/api/catedra/docentes/*/baja").hasRole(JEFE_CATEDRA)
                        .requestMatchers(HttpMethod.POST, "/api/catedra/comisiones-lectivas/*/docentes")
                        .hasRole(JEFE_CATEDRA)

                        // Los contratos internos exigen JWT, pero no un rol público.
                        .requestMatchers("/internal/**").authenticated()

                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorJwtKeycloak)));

        return http.build();
    }
}
