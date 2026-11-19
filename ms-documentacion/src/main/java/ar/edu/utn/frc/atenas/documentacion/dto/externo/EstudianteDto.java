package ar.edu.utn.frc.atenas.documentacion.dto.externo;

public record EstudianteDto(Long estudianteId, String sub, Boolean activo,
                           Long comisionLectivaId, Long cursoLectivoId, Long equipoId) { }