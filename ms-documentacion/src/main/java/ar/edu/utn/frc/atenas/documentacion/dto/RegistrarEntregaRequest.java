package ar.edu.utn.frc.atenas.documentacion.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarEntregaRequest(@NotBlank String enlace) { }