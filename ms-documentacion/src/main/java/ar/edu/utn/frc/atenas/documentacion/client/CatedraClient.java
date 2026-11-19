package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoInterno;

/**
 * Adaptador hacia ms-catedra. En C-E2 solo se usa obtenerCurso (I05). La
 * implementacion concreta (RestClient) la aporta D-E1.
 */
public interface CatedraClient {
    CursoLectivoInterno obtenerCurso(Long id);
    ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteInterno obtenerEstudiantePorSub(String sub);
    ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoInterno obtenerEquipo(Long id);
}