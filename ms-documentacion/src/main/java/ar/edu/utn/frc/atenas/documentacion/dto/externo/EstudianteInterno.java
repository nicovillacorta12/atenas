package ar.edu.utn.frc.atenas.documentacion.dto.externo;

/**
 * DTO minimo del estudiante provisto por ms-catedra (I03).
 */
public record EstudianteInterno(Long estudianteId, String sub, Boolean activo,
                                Long comisionLectivaId, Long cursoLectivoId, Long equipoId) { }