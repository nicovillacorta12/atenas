package ar.edu.utn.frc.atenas.documentacion.service;

import ar.edu.utn.frc.atenas.commons.error.AccesoDenegadoException;
import ar.edu.utn.frc.atenas.commons.error.NoEncontradoException;
import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.seguridad.Roles;
import ar.edu.utn.frc.atenas.commons.seguridad.UsuarioActual;
import ar.edu.utn.frc.atenas.commons.tiempo.ZonaHoraria;
import ar.edu.utn.frc.atenas.documentacion.client.CatedraClient;
import ar.edu.utn.frc.atenas.documentacion.dto.CrearHitoRequest;
import ar.edu.utn.frc.atenas.documentacion.dto.HitoResponse;
import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import ar.edu.utn.frc.atenas.documentacion.repository.HitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Reglas de hitos: solo el jefe crea; las fechas deben caer dentro del periodo
 * del curso lectivo (consulta a ms-catedra).
 */
@Service
public class HitoService {

    private final HitoRepository hitoRepository;
    private final CatedraClient catedraClient;
    private final UsuarioActual usuarioActual;
    private final ZonaHoraria zonaHoraria;

    public HitoService(HitoRepository hitoRepository, CatedraClient catedraClient,
                       UsuarioActual usuarioActual, ZonaHoraria zonaHoraria) {
        this.hitoRepository = hitoRepository;
        this.catedraClient = catedraClient;
        this.usuarioActual = usuarioActual;
        this.zonaHoraria = zonaHoraria;
    }

    @Transactional
    public HitoResponse crear(CrearHitoRequest request) {
        if (!usuarioActual.tieneRol(Roles.JEFE_CATEDRA)) {
            throw new AccesoDenegadoException("Solo el jefe puede definir hitos");
        }
        var curso = obtenerCurso(request.cursoLectivoId());

        Instant inicioCurso = curso.fechaInicio().atStartOfDay(ZoneId.of("America/Argentina/Cordoba")).toInstant();
        Instant finCurso = curso.fechaFin().plusDays(1).atStartOfDay(ZoneId.of("America/Argentina/Cordoba")).toInstant();

        if (!request.fechaInicio().isBefore(request.fechaLimite())) {
            throw new ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException("fechaInicio debe ser anterior a fechaLimite");
        }
        if (request.fechaInicio().isBefore(inicioCurso) || request.fechaLimite().isAfter(finCurso)) {
            throw new ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException("Las fechas deben estar dentro del periodo del curso");
        }

        Hito hito = new Hito();
        hito.setCursoLectivoId(request.cursoLectivoId());
        hito.setNombre(request.nombre());
        hito.setTipo(request.tipo());
        hito.setOrden(request.orden());
        hito.setFechaInicio(request.fechaInicio());
        hito.setFechaLimite(request.fechaLimite());
        Hito guardado = hitoRepository.save(hito);
        return aResponse(guardado);
    }

    @Transactional(readOnly = true)
    public java.util.List<HitoResponse> listar(Long cursoLectivoId) {
        if (cursoLectivoId == null) {
            throw new ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException("cursoLectivoId es obligatorio");
        }
        return hitoRepository.findByCursoLectivoIdOrderByOrdenAsc(cursoLectivoId).stream()
                .map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<HitoResponse> internos(Long cursoLectivoId, String tipo) {
        var lista = hitoRepository.findByCursoLectivoIdOrderByOrdenAsc(cursoLectivoId);
        if (tipo != null && !tipo.isBlank()) {
            Hito.Tipo t = Hito.Tipo.valueOf(tipo);
            lista = lista.stream().filter(h -> h.getTipo() == t).toList();
        }
        return lista.stream().map(this::aResponse).toList();
    }

    private ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoDto obtenerCurso(Long id) {
        try {
            return catedraClient.obtenerCurso(id);
        } catch (NoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            // si es NoEncontrado, se propaga; si es ServicioNoDisponible, tambien
            if (e instanceof ServicioNoDisponibleException || e instanceof NoEncontradoException) {
                throw (RuntimeException) e;
            }
            throw new ServicioNoDisponibleException("ms-catedra no responde");
        }
    }

    private HitoResponse aResponse(Hito h) {
        return new HitoResponse(h.getId(), h.getCursoLectivoId(), h.getNombre(), h.getTipo(), h.getOrden(),
                zonaHoraria.aOffsetDateTime(h.getFechaInicio()), zonaHoraria.aOffsetDateTime(h.getFechaLimite()));
    }
}