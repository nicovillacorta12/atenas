package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoInterno;
import org.springframework.stereotype.Component;

/**
 * Implementacion provisional de CatedraClient. Sera reemplazada por la
 * implementacion real con RestClient en D-E1.
 */
@Component
public class CatedraClientImpl implements CatedraClient {
    @Override
    public CursoLectivoInterno obtenerCurso(Long id) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteInterno obtenerEstudiantePorSub(String sub) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }

    @Override
    public ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoInterno obtenerEquipo(Long id) {
        throw new ServicioNoDisponibleException("CatedraClient no implementado (pendiente D-E1)");
    }
}