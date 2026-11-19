package ar.edu.utn.frc.atenas.documentacion.dto.externo;

import java.util.List;

/**
 * DTO minimo del equipo provisto por ms-catedra (I01/I02).
 */
public record EquipoInterno(Long id, String nombre, Long comisionLectivaId, Long cursoLectivoId,
                            String estado, Boolean asegurado, String representanteSub,
                            List<IntegranteInterno> integrantes) {
    public record IntegranteInterno(Long estudianteId, String sub, String legajo, String usuarioGithub) { }
}