package ar.edu.utn.frc.atenas.catedra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiCatedra() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de ms-catedra")
                        .version("1.0.0")
                        .description("Estructura académica, personas y equipos de la cátedra"));
    }
}
