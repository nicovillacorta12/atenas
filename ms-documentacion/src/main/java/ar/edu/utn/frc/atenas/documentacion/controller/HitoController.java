package ar.edu.utn.frc.atenas.documentacion.controller;

import ar.edu.utn.frc.atenas.documentacion.dto.CrearHitoRequest;
import ar.edu.utn.frc.atenas.documentacion.dto.HitoResponse;
import ar.edu.utn.frc.atenas.documentacion.service.HitoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/documentacion/hitos")
public class HitoController {

    private final HitoService hitoService;

    public HitoController(HitoService hitoService) {
        this.hitoService = hitoService;
    }

    @PostMapping
    public ResponseEntity<HitoResponse> crear(@Valid @RequestBody CrearHitoRequest request) {
        HitoResponse response = hitoService.crear(request);
        return ResponseEntity.created(URI.create("/api/documentacion/hitos/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<HitoResponse>> listar(@RequestParam(required = false) Long cursoLectivoId) {
        return ResponseEntity.ok(hitoService.listar(cursoLectivoId));
    }
}