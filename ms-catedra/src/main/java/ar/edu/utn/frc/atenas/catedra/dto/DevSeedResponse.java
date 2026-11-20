package ar.edu.utn.frc.atenas.catedra.dto;

import java.util.Map;

/**
 * Resultado del seed de E1 usado por Postman.
 *
 * Incluye los IDs que la colección guarda en el environment y los JWT de los
 * usuarios de prueba. Es un contrato exclusivamente académico/local.
 */
public record DevSeedResponse(
        Long cursoLectivoId,
        Long comisionId,
        Long comisionLectivaId,
        Long comision2Id,
        Long comisionLectiva2Id,
        Long docenteId,
        Long docenteBId,
        Long estudianteRepresentanteId,
        Long estudianteIntegranteId,
        Long estudianteBId,
        String estudianteSub,
        Map<String, String> tokens) {
}
