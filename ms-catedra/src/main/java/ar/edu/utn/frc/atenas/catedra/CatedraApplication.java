package ar.edu.utn.frc.atenas.catedra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {
        "ar.edu.utn.frc.atenas.catedra",
        "ar.edu.utn.frc.atenas.commons"
})
public class CatedraApplication {

    private static final Logger log = LoggerFactory.getLogger(CatedraApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext contexto = SpringApplication.run(CatedraApplication.class, args);

        // Se informa recién cuando el contexto terminó de iniciar, no cuando
        // Tomcat recién abre el puerto. Así se distingue "arrancando" de "listo".
        String nombre = contexto.getEnvironment().getProperty("spring.application.name", "aplicacion");
        String puerto = contexto.getEnvironment().getProperty("server.port", "8080");
        log.info("=================================================");
        log.info(" {} LISTA - escuchando en el puerto {}", nombre, puerto);
        log.info("=================================================");
    }
}
