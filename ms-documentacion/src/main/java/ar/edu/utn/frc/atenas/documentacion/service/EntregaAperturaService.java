package ar.edu.utn.frc.atenas.documentacion.service;

import ar.edu.utn.frc.atenas.commons.error.AccesoDenegadoException;
import ar.edu.utn.frc.atenas.commons.error.NoEncontradoException;
import ar.edu.utn.frc.atenas.commons.seguridad.UsuarioActual;
import ar.edu.utn.frc.atenas.commons.tiempo.ZonaHoraria;
import ar.edu.utn.frc.atenas.documentacion.client.ArchivoDrive;
import ar.edu.utn.frc.atenas.documentacion.client.CatedraClient;
import ar.edu.utn.frc.atenas.documentacion.client.DriveClient;
import ar.edu.utn.frc.atenas.documentacion.dto.CambioPostEntrega;
import ar.edu.utn.frc.atenas.documentacion.dto.EntregaAperturaResponse;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoDto;
import ar.edu.utn.frc.atenas.documentacion.entity.ArchivoVerificado;
import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import ar.edu.utn.frc.atenas.documentacion.entity.EstadoEntrega;
import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import ar.edu.utn.frc.atenas.documentacion.repository.ArchivoVerificadoRepository;
import ar.edu.utn.frc.atenas.documentacion.repository.EntregaRepository;
import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import org.springframework.stereotype.Service;

/**
 * Orquesta D06: abrir una entrega, validar pertenencia del docente y calcular
 * el aviso de cambios posteriores.
 *
 * Si Drive falla, la operación sigue siendo exitosa y devuelve NO_VERIFICADO.
 * Los errores de ms-catedra, en cambio, no permiten validar permisos y se
 * propagan como 503.
 */
@Service
public class EntregaAperturaService {

    private final CatedraClient catedraClient;
    private final EntregaRepository entregaRepository;
    private final ArchivoVerificadoRepository archivoVerificadoRepository;
    private final DriveClient driveClient;
    private final UsuarioActual usuarioActual;
    private final ZonaHoraria zonaHoraria;

    public EntregaAperturaService(CatedraClient catedraClient,
                                  EntregaRepository entregaRepository,
                                  ArchivoVerificadoRepository archivoVerificadoRepository,
                                  DriveClient driveClient,
                                  UsuarioActual usuarioActual,
                                  ZonaHoraria zonaHoraria) {
        this.catedraClient = catedraClient;
        this.entregaRepository = entregaRepository;
        this.archivoVerificadoRepository = archivoVerificadoRepository;
        this.driveClient = driveClient;
        this.usuarioActual = usuarioActual;
        this.zonaHoraria = zonaHoraria;
    }

    public EntregaAperturaResponse abrir(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new NoEncontradoException(
                        "No existe la entrega " + entregaId));

        EquipoDto equipo = catedraClient.obtenerEquipo(entrega.getEquipoId());
        validarDocenteAsignado(equipo.comisionLectivaId());

        ArchivoVerificado archivo = archivoVerificadoRepository
                .findByEntregaId(entregaId)
                .orElse(null);

        CambioPostEntrega cambio = calcularCambio(entrega, archivo);
        return aResponse(entrega, archivo, cambio);
    }

    private void validarDocenteAsignado(Long comisionLectivaId) {
        String sub = usuarioActual.sub();
        if (sub == null || sub.isBlank()
                || !catedraClient.docenteAsignado(comisionLectivaId, sub)) {
            throw new AccesoDenegadoException(
                    "El docente no está asignado a la comisión", "RN-01");
        }
    }

    private CambioPostEntrega calcularCambio(Entrega entrega, ArchivoVerificado archivo) {
        if (entrega.getEstado() == EstadoEntrega.CORREGIDA) {
            // El valor se congeló al corregir; Drive no interviene.
            if (entrega.getModificadoPostEntrega() == null) {
                return CambioPostEntrega.NO_VERIFICADO;
            }
            return entrega.getModificadoPostEntrega()
                    ? CambioPostEntrega.SI
                    : CambioPostEntrega.NO;
        }

        if (archivo == null
                || archivo.getDriveFileId() == null
                || archivo.getDriveFileId().isBlank()
                || archivo.getModifiedTimeEntrega() == null) {
            return CambioPostEntrega.NO_VERIFICADO;
        }

        try {
            ArchivoDrive actual = driveClient.obtenerArchivo(archivo.getDriveFileId());
            if (actual.modifiedTime() == null) {
                return CambioPostEntrega.NO_VERIFICADO;
            }

            boolean cambio = actual.modifiedTime().isAfter(archivo.getModifiedTimeEntrega())
                    && actual.modifiedTime().isAfter(entrega.getHito().getFechaLimite());
            return cambio ? CambioPostEntrega.SI : CambioPostEntrega.NO;
        } catch (NoEncontradoException | ServicioNoDisponibleException excepcion) {
            // RN-30: Drive es informativo; una falla no bloquea la apertura.
            return CambioPostEntrega.NO_VERIFICADO;
        }
    }

    private EntregaAperturaResponse aResponse(Entrega entrega,
                                               ArchivoVerificado archivo,
                                               CambioPostEntrega cambio) {
        Hito hito = entrega.getHito();
        return new EntregaAperturaResponse(
                entrega.getId(),
                new EntregaAperturaResponse.HitoApertura(
                        hito.getId(),
                        hito.getNombre(),
                        hito.getTipo().name(),
                        zonaHoraria.aOffsetDateTime(hito.getFechaInicio()),
                        zonaHoraria.aOffsetDateTime(hito.getFechaLimite())),
                entrega.getEquipoId(),
                entrega.getEstado().name(),
                entrega.getTardia(),
                entrega.getNota(),
                archivo != null ? archivo.getEnlace() : null,
                cambio);
    }
}
