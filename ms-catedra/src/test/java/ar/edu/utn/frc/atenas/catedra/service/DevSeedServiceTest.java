package ar.edu.utn.frc.atenas.catedra.service;

import ar.edu.utn.frc.atenas.catedra.client.KeycloakAdminClient;
import ar.edu.utn.frc.atenas.catedra.client.KeycloakDevTokenClient;
import ar.edu.utn.frc.atenas.catedra.config.KeycloakProperties;
import ar.edu.utn.frc.atenas.catedra.dto.DevSeedResponse;
import ar.edu.utn.frc.atenas.catedra.entity.Comision;
import ar.edu.utn.frc.atenas.catedra.entity.ComisionLectiva;
import ar.edu.utn.frc.atenas.catedra.entity.CursoLectivo;
import ar.edu.utn.frc.atenas.catedra.entity.Docente;
import ar.edu.utn.frc.atenas.catedra.entity.Estudiante;
import ar.edu.utn.frc.atenas.catedra.repository.AsignacionDocenteRepository;
import ar.edu.utn.frc.atenas.catedra.repository.ComisionLectivaRepository;
import ar.edu.utn.frc.atenas.catedra.repository.ComisionRepository;
import ar.edu.utn.frc.atenas.catedra.repository.CursoLectivoRepository;
import ar.edu.utn.frc.atenas.catedra.repository.DocenteRepository;
import ar.edu.utn.frc.atenas.catedra.repository.EquipoRepository;
import ar.edu.utn.frc.atenas.catedra.repository.EstudianteRepository;
import ar.edu.utn.frc.atenas.catedra.repository.IntegranteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevSeedServiceTest {

    @Mock
    private CursoLectivoRepository cursos;
    @Mock
    private ComisionRepository comisiones;
    @Mock
    private ComisionLectivaRepository comisionesLectivas;
    @Mock
    private DocenteRepository docentes;
    @Mock
    private EstudianteRepository estudiantes;
    @Mock
    private AsignacionDocenteRepository asignaciones;
    @Mock
    private EquipoRepository equipos;
    @Mock
    private IntegranteRepository integrantes;
    @Mock
    private KeycloakAdminClient keycloakAdmin;
    @Mock
    private KeycloakDevTokenClient keycloakTokens;

    private DevSeedService service;

    @BeforeEach
    void preparar() {
        KeycloakProperties propiedades = new KeycloakProperties();
        propiedades.setClaveTemporal("clave-temporal");
        propiedades.setJefePasswordInicial("clave-jefe");
        service = new DevSeedService(cursos, comisiones, comisionesLectivas,
                docentes, estudiantes, asignaciones, equipos, integrantes,
                keycloakAdmin, keycloakTokens, propiedades);
        when(keycloakTokens.obtenerToken(anyString(), anyString()))
                .thenAnswer(invocacion -> "token-" + invocacion.getArgument(0));
    }

    @Test
    @DisplayName("Seed E1 reutiliza los datos existentes y devuelve todos los tokens")
    void deberiaReutilizarE1YDevolverTokens() {
        CursoLectivo curso = new CursoLectivo(1L, 2026,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15));
        Comision comision1 = new Comision(1L, "Comisión 1");
        Comision comision2 = new Comision(2L, "Comisión 2");
        ComisionLectiva cl1 = new ComisionLectiva(10L, comision1, curso);
        ComisionLectiva cl2 = new ComisionLectiva(20L, comision2, curso);
        Docente docenteA = new Docente(1L, "Docente", "A", "docente-a@atenas.local", "sub-a", true);
        Docente docenteB = new Docente(2L, "Docente", "B", "docente-b@atenas.local", "sub-b", true);
        Estudiante representante = new Estudiante(1L, "400001", "Alumno", "Representante",
                "400001@sistemas.frc.utn.edu.ar", "sub-rep", null, cl1, true);
        Estudiante integrante = new Estudiante(2L, "400002", "Alumno", "Integrante",
                "400002@sistemas.frc.utn.edu.ar", "sub-int", null, cl1, true);
        Estudiante alumnoB = new Estudiante(3L, "400003", "Alumno", "B",
                "400003@sistemas.frc.utn.edu.ar", "sub-b", null, cl1, true);

        when(cursos.findByAnio(2026)).thenReturn(Optional.of(curso));
        when(comisiones.findByNombre("Comisión 1")).thenReturn(Optional.of(comision1));
        when(comisiones.findByNombre("Comisión 2")).thenReturn(Optional.of(comision2));
        when(comisionesLectivas.findByComisionIdAndCursoLectivoId(1L, 1L)).thenReturn(Optional.of(cl1));
        when(comisionesLectivas.findByComisionIdAndCursoLectivoId(2L, 1L)).thenReturn(Optional.of(cl2));
        when(docentes.findByMail("docente-a@atenas.local")).thenReturn(Optional.of(docenteA));
        when(docentes.findByMail("docente-b@atenas.local")).thenReturn(Optional.of(docenteB));
        when(estudiantes.findByLegajo("400001")).thenReturn(Optional.of(representante));
        when(estudiantes.findByLegajo("400002")).thenReturn(Optional.of(integrante));
        when(estudiantes.findByLegajo("400003")).thenReturn(Optional.of(alumnoB));
        when(asignaciones.existsByDocenteIdAndComisionLectivaId(1L, 10L)).thenReturn(true);
        when(asignaciones.existsByDocenteIdAndComisionLectivaId(1L, 20L)).thenReturn(true);

        DevSeedResponse respuesta = service.seedE1();

        assertThat(respuesta.cursoLectivoId()).isEqualTo(1L);
        assertThat(respuesta.comisionLectivaId()).isEqualTo(10L);
        assertThat(respuesta.estudianteRepresentanteId()).isEqualTo(1L);
        assertThat(respuesta.estudianteSub()).isEqualTo("sub-rep");
        assertThat(respuesta.tokens()).containsKeys(
                "jefe", "docente-a", "docente-b", "representante", "integrante", "alumno-b");
        assertThat(respuesta.tokens().get("jefe")).isEqualTo("token-jefe");
        verify(keycloakAdmin).prepararUsuarioParaPruebas("sub-a", "clave-temporal");
        verify(keycloakAdmin).prepararUsuarioParaPruebas("sub-rep", "clave-temporal");
    }
}
