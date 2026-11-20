package ar.edu.utn.frc.atenas.catedra.controller;

import ar.edu.utn.frc.atenas.catedra.dto.DevSeedResponse;
import ar.edu.utn.frc.atenas.catedra.service.DevSeedService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint académico para preparar E1 y devolver todos los JWT de Postman.
 *
 * Solo lo puede invocar un jefe autenticado. No forma parte del flujo público
 * de negocio; existe para acelerar las pruebas end-to-end locales.
 */
@RestController
@RequestMapping("/api/catedra/dev")
public class DevSeedController {

    private final DevSeedService devSeedService;

    public DevSeedController(DevSeedService devSeedService) {
        this.devSeedService = devSeedService;
    }

    @PostMapping("/seed-e1")
    public DevSeedResponse seedE1() {
        return devSeedService.seedE1();
    }
}
