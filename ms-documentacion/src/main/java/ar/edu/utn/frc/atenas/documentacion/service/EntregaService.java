package ar.edu.utn.frc.atenas.documentacion.service;

import ar.edu.utn.frc.atenas.commons.error.AccesoDenegadoException;
import ar.edu.utn.frc.atenas.commons.error.ConflictoException;
import ar.edu.utn.frc.atenas.commons.error.NoEncontradoException;
import ar.edu.utn.frc.atenas.commons.error.NoProcesableException;
import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException;
import ar.edu.utn.frc.atenas.commons.seguridad.Roles;
import ar.edu.utn.frc.atenas.commons.seguridad.UsuarioActual;
import ar.edu.utn.frc.atenas.commons.tiempo.ZonaHoraria;
import ar.edu.utn.frc.atenas.documentacion.client.CatedraClient;
import ar.edu.utn.frc.atenas.documentacion.client.DriveClient;
import ar.edu.utn.frc.atenas.documentacion.dto.RegistrarEntregaRequest;
import ar.edu.utn.frc.atenas.documentacion.dto.EntregaResponse;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDrive;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoDto;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteDto;
import ar.edu.utn.frc.atenas.documentacion.entity.ArchivoVerificado;
import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import ar.edu.utn.frc.atenas.documentacion.exception.ArchivoNoAccesibleException;
import ar.edu.utn.frc.atenas.documentacion.repository.ArchivoVerificadoRepository;
import ar.edu.utn.frc.atenas.documentacion.repository.EntregaRepository;
import ar.edu.utn.frc.atenas.documentacion.repository.HitoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

/**
 * Registra una entrega verificada en Drive (RN18-RN22, RN29). Las consultas a
 * ms-catedra y Drive ocurren ANTES de abrir la transaccion; la persistencia de
 * entrega + archivo_verificado es atomica.
 */
@Service
public class EntregaService {

    private final HitoRepository hitoRepository;
    private final EntregaRepository entregaRepository;
    private final ArchivoVerificadoRepository archivoRepository;
    private final CatedraClient catedraClient;
    private final DriveClient driveClient;
    private final UsuarioActual usuarioActual;
    private final ZonaHoraria zonaHoraria;
    private final TransactionTemplate transactionTemplate;

    public EntregaService(HitoRepository hitoRepository, EntregaRepository entregaRepository,
                          ArchivoVerificadoRepository archivoRepository, CatedraClient catedraClient,
                          DriveClient driveClient, UsuarioActual usuarioActual, ZonaHoraria zonaHoraria,
                          PlatformTransactionManager txManager) {
        this.hitoRepository = hitoRepository;
        this.entregaRepository = entregaRepository;
        this.archivoRepository = archivoRepository;
        this.catedraClient = catedraClient;
        this.driveClient = driveClient;
        this.usuarioActual = usuarioActual;
        this.zonaHoraria = zonaHoraria;
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    public EntregaResponse registrar(Long hitoId, RegistrarEntregaRequest request) {
        if (!usuarioActual.tieneRol(Roles.ALUMNO)) {
            throw new AccesoDenegadoException("Solo un alumno puede registrar una entrega");
        }
        String sub = usuarioActual.sub();

        // 1. hito existe
        Hito hito = hitoRepository.findById(hitoId)
                .orElseThrow(() -> new NoEncontradoException("Hito no encontrado"));

        // 2. ms-catedra
        EstudianteDto estudiante;
        EquipoDto equipo;
        try {
            estudiante = catedraClient.obtenerEstudiantePorSub(sub);
            if (estudiante.equipoId() == null) {
                throw new AccesoDenegadoException("El alumno no tiene equipo");
            }
            equipo = catedraClient.obtenerEquipo(estudiante.equipoId());
        } catch (NoEncontradoException | ServicioNoDisponibleException e) {
            throw e;
        } catch (AccesoDenegadoException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServicioNoDisponibleException("ms-catedra no responde");
        }

        if (!sub.equals(equipo.representanteSub())) {
            throw new AccesoDenegadoException("Solo el representante puede registrar la entrega");
        }
        if (!hito.getCursoLectivoId().equals(equipo.cursoLectivoId())) {
            throw new AccesoDenegadoException("El hito no pertenece al curso del equipo");
        }
        if (equipo.asegurado() == null || !equipo.asegurado()) {
            throw new ConflictoException("El equipo no esta asegurado");
        }

        // 3. duplicado
        if (entregaRepository.findByHitoIdAndEquipoId(hitoId, equipo.id()).isPresent()) {
            throw new ConflictoException("Ya existe una entrega para este hito y equipo");
        }

        // 4. Drive
        String fileId;
        ArchivoDrive archivo;
        try {
            fileId = driveClient.extraerFileId(request.enlace());
        } catch (SolicitudInvalidaException e) {
            throw e;
        }
        try {
            archivo = driveClient.obtenerArchivo(fileId);
        } catch (ArchivoNoAccesibleException e) {
            throw new NoProcesableException("Archivo no accesible");
        } catch (ServicioNoDisponibleException e) {
            throw e;
        }
        String mime = archivo.mimeType();
        if (!"application/pdf".equals(mime) && !"application/vnd.google-apps.document".equals(mime)) {
            throw new NoProcesableException("Tipo de archivo no permitido");
        }

        // 5. persistir en transaccion
        Instant ahora = Instant.now();
        boolean tardia = ahora.isAfter(hito.getFechaLimite());

        Entrega guardada;
        try {
            guardada = transactionTemplate.execute(status -> {
                Entrega entrega = new Entrega();
                entrega.setHitoId(hitoId);
                entrega.setEquipoId(equipo.id());
                entrega.setEstado(Entrega.Estado.ENTREGADA);
                entrega.setTardia(tardia);
                entrega.setFechaEntrega(ahora);
                entrega.setRegistradaPorSub(sub);
                Entrega e = entregaRepository.saveAndFlush(entrega);

                ArchivoVerificado av = new ArchivoVerificado();
                av.setEntregaId(e.getId());
                av.setEnlace(request.enlace());
                av.setDriveFileId(archivo.fileId());
                av.setNombre(archivo.nombre());
                av.setMimeType(archivo.mimeType());
                av.setModifiedTimeEntrega(archivo.modifiedTime());
                archivoRepository.save(av);
                return e;
            });
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException("Ya existe una entrega para este hito y equipo");
        }

        return new EntregaResponse(guardada.getId(), guardada.getHitoId(), guardada.getEquipoId(),
                guardada.getEstado(), guardada.getTardia(), zonaHoraria.aOffsetDateTime(guardada.getFechaEntrega()),
                request.enlace(), null);
    }
}