package ar.edu.utn.frc.atenas.documentacion.controller;

import ar.edu.utn.frc.atenas.documentacion.dto.EntregaResponse;
import ar.edu.utn.frc.atenas.documentacion.dto.RegistrarEntregaRequest;
import ar.edu.utn.frc.atenas.documentacion.service.EntregaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/documentacion/hitos")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping("/{id}/entregas")
    public ResponseEntity<EntregaResponse> registrar(@PathVariable Long id,
                                                     @Valid @RequestBody RegistrarEntregaRequest request) {
        EntregaResponse response = entregaService.registrar(id, request);
        return ResponseEntity.created(URI.create("/api/documentacion/entregas/" + response.id())).body(response);
    }
}