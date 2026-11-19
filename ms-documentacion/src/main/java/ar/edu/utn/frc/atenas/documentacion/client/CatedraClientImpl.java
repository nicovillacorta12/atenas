package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoDto;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoDto;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatedraClientImpl implements CatedraClient {
    @Override
    public EquipoDto obtenerEquipo(Long equipoId) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public List<EquipoDto> listarEquipos(Long comisionLectivaId, Long cursoLectivoId) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public EstudianteDto obtenerEstudiantePorSub(String sub) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public boolean docenteAsignado(Long comisionLectivaId, String sub) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public CursoLectivoDto obtenerCurso(Long cursoLectivoId) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }
}