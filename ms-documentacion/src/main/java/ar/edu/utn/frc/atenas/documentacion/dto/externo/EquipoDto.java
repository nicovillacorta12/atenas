package ar.edu.utn.frc.atenas.documentacion.dto.externo;

import java.util.List;

public record EquipoDto(Long id, String nombre, Long comisionLectivaId, Long cursoLectivoId,
                        String estado, Boolean asegurado, String representanteSub,
                        List<IntegranteDto> integrantes) {
    public record IntegranteDto(Long estudianteId, String sub, String legajo, String usuarioGithub) { }
}