package ar.edu.utn.frc.atenas.documentacion.service;

import ar.edu.utn.frc.atenas.commons.error.AccesoDenegadoException;
import ar.edu.utn.frc.atenas.commons.error.NoEncontradoException;
import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException;
import ar.edu.utn.frc.atenas.commons.seguridad.Roles;
import ar.edu.utn.frc.atenas.commons.seguridad.UsuarioActual;
import ar.edu.utn.frc.atenas.commons.tiempo.ZonaHoraria;
import ar.edu.utn.frc.atenas.documentacion.client.CatedraClient;
import ar.edu.utn.frc.atenas.documentacion.dto.CrearHitoRequest;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.CursoLectivoDto;
import ar.edu.utn.frc.atenas.documentacion.entity.TipoHito;
import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import ar.edu.utn.frc.atenas.documentacion.repository.HitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HitoServiceTest {

    @Mock private HitoRepository hitoRepository;
    @Mock private CatedraClient catedraClient;
    @Mock private UsuarioActual usuarioActual;
    @Mock private ZonaHoraria zonaHoraria;

    private HitoService service;

    @BeforeEach
    void setup() {
        service = new HitoService(hitoRepository, catedraClient, usuarioActual, zonaHoraria);
        when(zonaHoraria.aOffsetDateTime(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenAnswer(inv -> ((Instant) inv.getArgument(0)).atOffset(ZoneOffset.of("-03:00")));
    }

    private CursoLectivoDto curso() {
        return new CursoLectivoDto(2L, 2026, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15));
    }

    @Test
    void crear_ok() {
        when(usuarioActual.tieneRol(Roles.JEFE_CATEDRA)).thenReturn(true);
        when(catedraClient.obtenerCurso(2L)).thenReturn(curso());
        when(hitoRepository.save(org.mockito.ArgumentMatchers.any(Hito.class))).thenAnswer(inv -> {
            Hito h = inv.getArgument(0);
            h.setId(10L);
            return h;
        });
        CrearHitoRequest req = new CrearHitoRequest(2L, "Sprint 1", TipoHito.SPRINT, 1,
                Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-30T00:00:00Z"));
        var res = service.crear(req);
        assertThat(res.id()).isEqualTo(10L);
    }

    @Test
    void crear_noJefe() {
        when(usuarioActual.tieneRol(Roles.JEFE_CATEDRA)).thenReturn(false);
        CrearHitoRequest req = new CrearHitoRequest(2L, "Sprint 1", TipoHito.SPRINT, 1,
                Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-30T00:00:00Z"));
        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void crear_cursoInexistente() {
        when(usuarioActual.tieneRol(Roles.JEFE_CATEDRA)).thenReturn(true);
        when(catedraClient.obtenerCurso(2L)).thenThrow(new NoEncontradoException("no existe"));
        CrearHitoRequest req = new CrearHitoRequest(2L, "Sprint 1", TipoHito.SPRINT, 1,
                Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-30T00:00:00Z"));
        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void crear_fechasInvertidas() {
        when(usuarioActual.tieneRol(Roles.JEFE_CATEDRA)).thenReturn(true);
        when(catedraClient.obtenerCurso(2L)).thenReturn(curso());
        CrearHitoRequest req = new CrearHitoRequest(2L, "Sprint 1", TipoHito.SPRINT, 1,
                Instant.parse("2026-05-01T00:00:00Z"), Instant.parse("2026-04-30T00:00:00Z"));
        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crear_msCatedraCaido() {
        when(usuarioActual.tieneRol(Roles.JEFE_CATEDRA)).thenReturn(true);
        when(catedraClient.obtenerCurso(2L)).thenThrow(new ServicioNoDisponibleException("caido"));
        CrearHitoRequest req = new CrearHitoRequest(2L, "Sprint 1", TipoHito.SPRINT, 1,
                Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-30T00:00:00Z"));
        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(ServicioNoDisponibleException.class);
    }

    @Test
    void listar_cursoObligatorio() {
        assertThatThrownBy(() -> service.listar(null)).isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void internos_filtraPorTipo() {
        Hito h = new Hito();
        h.setId(1L);
        h.setCursoLectivoId(2L);
        h.setNombre("Sprint 1");
        h.setTipo(TipoHito.SPRINT);
        h.setOrden(1);
        h.setFechaInicio(Instant.parse("2026-04-01T00:00:00Z"));
        h.setFechaLimite(Instant.parse("2026-04-30T00:00:00Z"));
        when(hitoRepository.findByCursoLectivoIdOrderByOrdenAscIdAsc(2L)).thenReturn(List.of(h));
        var res = service.internos(2L, "SPRINT");
        assertThat(res).hasSize(1);
    }
}
