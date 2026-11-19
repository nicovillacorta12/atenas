package ar.edu.utn.frc.atenas.documentacion.dto.externo;

import java.time.LocalDate;

/**
 * DTO minimo del curso lectivo provisto por ms-catedra (I05). Los consumidores
 * ignoran propiedades desconocidas (ADR-006).
 */
public record CursoLectivoInterno(Long id, Integer anio, LocalDate fechaInicio, LocalDate fechaFin) { }