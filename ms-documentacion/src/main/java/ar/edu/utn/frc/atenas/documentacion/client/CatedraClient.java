package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoDto;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoDto;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteDto;

import java.util.List;

/**
 * Adaptador de salida hacia ms-catedra. Contratos I01 a I05 de docs/06.
 */
public interface CatedraClient {

    /** I01: equipo con estado, comisión, curso, representante e integrantes. */
    EquipoDto obtenerEquipo(Long equipoId);

    /**
     * I02: lista los equipos de una comisión lectiva o de un curso lectivo.
     * Se debe indicar exactamente uno de los dos parámetros.
     */
    List<EquipoDto> listarEquipos(Long comisionLectivaId, Long cursoLectivoId);

    /** I03: comisión, curso y equipo vigentes del alumno según su sub. */
    EstudianteDto obtenerEstudiantePorSub(String sub);

    /**
     * I04: indica si el docente está asignado a la comisión lectiva.
     */
    boolean docenteAsignado(Long comisionLectivaId, String sub);

    /** I05: curso lectivo y sus fechas. */
    CursoLectivoDto obtenerCurso(Long cursoLectivoId);
}