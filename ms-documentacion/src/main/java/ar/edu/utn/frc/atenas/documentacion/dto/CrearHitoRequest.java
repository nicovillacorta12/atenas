package ar.edu.utn.frc.atenas.documentacion.dto;

import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CrearHitoRequest(
        @NotNull Long cursoLectivoId,
        @NotBlank String nombre,
        @NotNull Hito.Tipo tipo,
        @NotNull Integer orden,
        @NotNull Instant fechaInicio,
        @NotNull Instant fechaLimite
) { }