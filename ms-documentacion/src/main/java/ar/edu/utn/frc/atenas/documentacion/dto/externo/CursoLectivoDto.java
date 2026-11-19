package ar.edu.utn.frc.atenas.documentacion.dto.externo;

import java.time.LocalDate;

public record CursoLectivoDto(Long id, Integer anio, LocalDate fechaInicio, LocalDate fechaFin) { }