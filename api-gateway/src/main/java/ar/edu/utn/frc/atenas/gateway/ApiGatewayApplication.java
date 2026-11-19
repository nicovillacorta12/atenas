package ar.edu.utn.frc.atenas.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext contexto = SpringApplication.run(ApiGatewayApplication.class, args);

        // Se informa recién cuando el contexto terminó de iniciar, no cuando
        // Netty recién abre el puerto. Así se distingue "arrancando" de "listo".
        String nombre = contexto.getEnvironment().getProperty("spring.application.name", "aplicacion");
        String puerto = contexto.getEnvironment().getProperty("server.port", "8080");
        log.info("=================================================");
        log.info(" {} LISTA - escuchando en el puerto {}", nombre, puerto);
        log.info("=================================================");
    }
}
