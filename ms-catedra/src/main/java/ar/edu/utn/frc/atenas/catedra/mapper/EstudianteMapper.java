package ar.edu.utn.frc.atenas.catedra.mapper;

import ar.edu.utn.frc.atenas.catedra.dto.EstudianteInternoResponse;
import ar.edu.utn.frc.atenas.catedra.dto.EstudianteResponse;
import ar.edu.utn.frc.atenas.catedra.dto.MiPerfilResponse;
import ar.edu.utn.frc.atenas.catedra.entity.ComisionLectiva;
import ar.edu.utn.frc.atenas.catedra.entity.Estudiante;

/**
 * Conversión manual entre {@link Estudiante} y los DTO públicos e internos.
 * El equipo todavía no existe; por eso los DTO lo informan como {@code null}.
 */
public final class EstudianteMapper {

    private EstudianteMapper() {
    }

    public static EstudianteResponse aResponse(Estudiante estudiante) {
        return new EstudianteResponse(
                estudiante.getId(),
                estudiante.getLegajo(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getMail(),
                estudiante.getUsuarioGithub(),
                estudiante.isActivo(),
                estudiante.getComisionLectiva().getId());
    }

    public static MiPerfilResponse aMiPerfil(Estudiante estudiante) {
        ComisionLectiva comisionLectiva = estudiante.getComisionLectiva();
        return new MiPerfilResponse(
                estudiante.getId(),
                estudiante.getLegajo(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getMail(),
                estudiante.getUsuarioGithub(),
                estudiante.isActivo(),
                comisionLectiva.getId(),
                comisionLectiva.getComision().getNombre(),
                comisionLectiva.getCursoLectivo().getId(),
                comisionLectiva.getCursoLectivo().getAnio(),
                null);
    }

    public static EstudianteInternoResponse aInterno(Estudiante estudiante) {
        ComisionLectiva comisionLectiva = estudiante.getComisionLectiva();
        return new EstudianteInternoResponse(
                estudiante.getId(),
                estudiante.getSub(),
                estudiante.isActivo(),
                comisionLectiva.getId(),
                comisionLectiva.getCursoLectivo().getId(),
                null);
    }
}
