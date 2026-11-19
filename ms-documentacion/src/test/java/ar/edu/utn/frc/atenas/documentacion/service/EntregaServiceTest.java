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
import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDrive;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EquipoInterno;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.EstudianteInterno;
import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import ar.edu.utn.frc.atenas.documentacion.repository.ArchivoVerificadoRepository;
import ar.edu.utn.frc.atenas.documentacion.repository.EntregaRepository;
import ar.edu.utn.frc.atenas.documentacion.repository.HitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntregaServiceTest {

    @Mock private HitoRepository hitoRepository;
    @Mock private EntregaRepository entregaRepository;
    @Mock private ArchivoVerificadoRepository archivoRepository;
    @Mock private CatedraClient catedraClient;
    @Mock private DriveClient driveClient;
    @Mock private UsuarioActual usuarioActual;
    @Mock private ZonaHoraria zonaHoraria;
    @Mock private PlatformTransactionManager txManager;

    private EntregaService service;

    @BeforeEach
    void setup() {
        org.mockito.Mockito.when(txManager.getTransaction(any())).thenReturn(new org.springframework.transaction.support.DefaultTransactionStatus(null, true, false, false, false, null));
        service = new EntregaService(hitoRepository, entregaRepository, archivoRepository, catedraClient, driveClient, usuarioActual, zonaHoraria, txManager);
        when(usuarioActual.tieneRol(Roles.ALUMNO)).thenReturn(true);
        when(usuarioActual.sub()).thenReturn("sub-alumno");
        when(zonaHoraria.aOffsetDateTime(any(Instant.class))).thenAnswer(inv -> ((Instant) inv.getArgument(0)).atOffset(ZoneOffset.of("-03:00")));
    }

    private Hito hito() {
        Hito h = new Hito();
        h.setId(3L);
        h.setCursoLectivoId(2L);
        h.setFechaLimite(Instant.parse("2026-12-31T23:59:59Z"));
        return h;
    }

    private EstudianteInterno estudiante(Long equipoId) {
        return new EstudianteInterno(5L, "sub-alumno", true, 10L, 2L, equipoId);
    }

    private EquipoInterno equipoAsegurado() {
        return new EquipoInterno(7L, "Eq", 10L, 2L, "ASEGURADO", true, "sub-alumno", List.of());
    }

    @Test
    void registrar_ok() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.empty());
        when(driveClient.extraerFileId("https://drive.google.com/file/d/F1/view")).thenReturn("F1");
        when(driveClient.obtenerArchivo("F1")).thenReturn(new ArchivoDrive("F1", "a.pdf", "application/pdf", Instant.now()));
        when(entregaRepository.saveAndFlush(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            e.setId(99L);
            return e;
        });
        var res = service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view"));
        assertThat(res.id()).isEqualTo(99L);
        assertThat(res.estado()).isEqualTo(Entrega.Estado.ENTREGADA);
    }

    @Test
    void registrar_noRepresentante() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        EquipoInterno eq = new EquipoInterno(7L, "Eq", 10L, 2L, "ASEGURADO", true, "otro", List.of());
        when(catedraClient.obtenerEquipo(7L)).thenReturn(eq);
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void registrar_noAsegurado() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        EquipoInterno eq = new EquipoInterno(7L, "Eq", 10L, 2L, "ABIERTO", false, "sub-alumno", List.of());
        when(catedraClient.obtenerEquipo(7L)).thenReturn(eq);
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void registrar_duplicado() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.of(new Entrega()));
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void registrar_archivoPrivado() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.empty());
        when(driveClient.extraerFileId("https://drive.google.com/file/d/F1/view")).thenReturn("F1");
        ar.edu.utn.frc.atenas.documentacion.exception.ArchivoNoAccesibleException ex =
                new ar.edu.utn.frc.atenas.documentacion.exception.ArchivoNoAccesibleException("privado");
        when(driveClient.obtenerArchivo("F1")).thenThrow(ex);
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(NoProcesableException.class);
    }

    @Test
    void registrar_tipoNoPermitido() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.empty());
        when(driveClient.extraerFileId("https://drive.google.com/file/d/F1/view")).thenReturn("F1");
        when(driveClient.obtenerArchivo("F1")).thenReturn(new ArchivoDrive("F1", "a.png", "image/png", Instant.now()));
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(NoProcesableException.class);
    }

    @Test
    void registrar_driveCaido() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.empty());
        when(driveClient.extraerFileId("https://drive.google.com/file/d/F1/view")).thenReturn("F1");
        when(driveClient.obtenerArchivo("F1")).thenThrow(new ServicioNoDisponibleException("caido"));
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    @Test
    void registrar_enlaceMalFormado() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenReturn(estudiante(7L));
        when(catedraClient.obtenerEquipo(7L)).thenReturn(equipoAsegurado());
        when(entregaRepository.findByHitoIdAndEquipoId(3L, 7L)).thenReturn(java.util.Optional.empty());
        when(driveClient.extraerFileId("mal")).thenThrow(new SolicitudInvalidaException("mal"));
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("mal")))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void registrar_hitoInexistente() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void registrar_msCatedraCaido() {
        when(hitoRepository.findById(3L)).thenReturn(java.util.Optional.of(hito()));
        when(catedraClient.obtenerEstudiantePorSub("sub-alumno")).thenThrow(new ServicioNoDisponibleException("caido"));
        assertThatThrownBy(() -> service.registrar(3L, new RegistrarEntregaRequest("https://drive.google.com/file/d/F1/view")))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}