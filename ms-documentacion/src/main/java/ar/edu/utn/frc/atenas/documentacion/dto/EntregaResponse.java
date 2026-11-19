package ar.edu.utn.frc.atenas.documentacion.dto;

import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;

import java.time.OffsetDateTime;

public record EntregaResponse(
        Long id,
        Long hitoId,
        Long equipoId,
        Entrega.Estado estado,
        Boolean tardia,
        OffsetDateTime fechaEntrega,
        String enlace,
        Long archivoId
) { }