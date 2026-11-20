package ar.edu.utn.frc.atenas.catedra.service;

import ar.edu.utn.frc.atenas.catedra.client.KeycloakAdminClient;
import ar.edu.utn.frc.atenas.catedra.client.KeycloakDevTokenClient;
import ar.edu.utn.frc.atenas.catedra.config.KeycloakProperties;
import ar.edu.utn.frc.atenas.catedra.dto.DevSeedResponse;
import ar.edu.utn.frc.atenas.catedra.entity.AsignacionDocente;
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
import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.seguridad.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seed académico de la carpeta 01 de Postman.
 *
 * Es idempotente: reutiliza curso, comisiones, docentes y estudiantes si ya
 * existen. No reemplaza las reglas de los services reales; solo prepara datos
 * de prueba y devuelve los JWT para no tener que generarlos uno por uno.
 */
@Service
public class DevSeedService {

    private static final int ANIO = 2026;
    private static final String COMISION_1 = "Comisión 1";
    private static final String COMISION_2 = "Comisión 2";
    private static final String MAIL_DOCENTE_A = "docente-a@atenas.local";
    private static final String MAIL_DOCENTE_B = "docente-b@atenas.local";

    private final CursoLectivoRepository cursos;
    private final ComisionRepository comisiones;
    private final ComisionLectivaRepository comisionesLectivas;
    private final DocenteRepository docentes;
    private final EstudianteRepository estudiantes;
    private final AsignacionDocenteRepository asignaciones;
    private final EquipoRepository equipos;
    private final IntegranteRepository integrantes;
    private final KeycloakAdminClient keycloakAdmin;
    private final KeycloakDevTokenClient keycloakTokens;
    private final KeycloakProperties keycloakProperties;

    public DevSeedService(CursoLectivoRepository cursos,
                          ComisionRepository comisiones,
                          ComisionLectivaRepository comisionesLectivas,
                          DocenteRepository docentes,
                          EstudianteRepository estudiantes,
                          AsignacionDocenteRepository asignaciones,
                          EquipoRepository equipos,
                          IntegranteRepository integrantes,
                          KeycloakAdminClient keycloakAdmin,
                          KeycloakDevTokenClient keycloakTokens,
                          KeycloakProperties keycloakProperties) {
        this.cursos = cursos;
        this.comisiones = comisiones;
        this.comisionesLectivas = comisionesLectivas;
        this.docentes = docentes;
        this.estudiantes = estudiantes;
        this.asignaciones = asignaciones;
        this.equipos = equipos;
        this.integrantes = integrantes;
        this.keycloakAdmin = keycloakAdmin;
        this.keycloakTokens = keycloakTokens;
        this.keycloakProperties = keycloakProperties;
    }

    @Transactional
    public DevSeedResponse seedE1() {
        // El seed deja E1 como punto de partida: elimina equipos e integrantes
        // de corridas anteriores para que 02/07 puedan repetirse sin conflictos.
        integrantes.deleteAllInBatch();
        equipos.deleteAllInBatch();

        CursoLectivo curso = cursos.findByAnio(ANIO)
                .orElseGet(() -> cursos.saveAndFlush(new CursoLectivo(
                        ANIO, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15))));

        Comision comision1 = comision(COMISION_1);
        Comision comision2 = comision(COMISION_2);

        ComisionLectiva comisionLectiva1 = comisionLectiva(comision1, curso);
        ComisionLectiva comisionLectiva2 = comisionLectiva(comision2, curso);

        Docente docenteA = docente("Docente", "A", MAIL_DOCENTE_A);
        Docente docenteB = docente("Docente", "B", MAIL_DOCENTE_B);
        asignar(docenteA, comisionLectiva1);
        asignar(docenteA, comisionLectiva2);

        Estudiante representante = estudiante(
                "400001", "Alumno", "Representante", comisionLectiva1);
        Estudiante integrante = estudiante(
                "400002", "Alumno", "Integrante", comisionLectiva1);
        Estudiante alumnoB = estudiante(
                "400003", "Alumno", "B", comisionLectiva1);

        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("jefe", token("jefe", keycloakProperties.getJefePasswordInicial()));
        tokens.put("docente-a", token(MAIL_DOCENTE_A, keycloakProperties.getClaveTemporal()));
        tokens.put("docente-b", token(MAIL_DOCENTE_B, keycloakProperties.getClaveTemporal()));
        tokens.put("representante", token("400001@sistemas.frc.utn.edu.ar",
                keycloakProperties.getClaveTemporal()));
        tokens.put("integrante", token("400002@sistemas.frc.utn.edu.ar",
                keycloakProperties.getClaveTemporal()));
        tokens.put("alumno-b", token("400003@sistemas.frc.utn.edu.ar",
                keycloakProperties.getClaveTemporal()));

        return new DevSeedResponse(
                curso.getId(),
                comision1.getId(),
                comisionLectiva1.getId(),
                comision2.getId(),
                comisionLectiva2.getId(),
                docenteA.getId(),
                docenteB.getId(),
                representante.getId(),
                integrante.getId(),
                alumnoB.getId(),
                representante.getSub(),
                tokens);
    }

    private Comision comision(String nombre) {
        return comisiones.findByNombre(nombre)
                .orElseGet(() -> comisiones.saveAndFlush(new Comision(nombre)));
    }

    private ComisionLectiva comisionLectiva(Comision comision, CursoLectivo curso) {
        return comisionesLectivas
                .findByComisionIdAndCursoLectivoId(comision.getId(), curso.getId())
                .orElseGet(() -> comisionesLectivas.saveAndFlush(
                        new ComisionLectiva(comision, curso)));
    }

    private Docente docente(String nombre, String apellido, String mail) {
        Docente docente = docentes.findByMail(mail)
                .orElseGet(() -> docentes.saveAndFlush(new Docente(nombre, apellido, mail)));

        String sub = asegurarUsuario(mail, nombre, apellido, Roles.DOCENTE, docente.getSub());
        if (!sub.equals(docente.getSub()) || !docente.isActivo()) {
            docente.setSub(sub);
            docente.setActivo(true);
            docente = docentes.saveAndFlush(docente);
        }
        return docente;
    }

    private Estudiante estudiante(String legajo, String nombre, String apellido,
                                  ComisionLectiva comisionLectiva) {
        Estudiante estudiante = estudiantes.findByLegajo(legajo)
                .orElseGet(() -> estudiantes.saveAndFlush(new Estudiante(
                        legajo, nombre, apellido, legajo + "@sistemas.frc.utn.edu.ar",
                        comisionLectiva)));

        String sub = asegurarUsuario(estudiante.getMail(), nombre, apellido,
                Roles.ALUMNO, estudiante.getSub());
        if (!sub.equals(estudiante.getSub()) || !estudiante.isActivo()) {
            estudiante.setSub(sub);
            estudiante.setActivo(true);
            estudiante = estudiantes.saveAndFlush(estudiante);
        }
        return estudiante;
    }

    private String asegurarUsuario(String mail, String nombre, String apellido,
                                   String rol, String subActual) {
        String sub = subActual;
        if (sub == null || sub.isBlank()) {
            sub = keycloakAdmin.buscarSubPorMail(mail)
                    .orElseGet(() -> keycloakAdmin.crearUsuario(
                            mail, nombre, apellido, keycloakProperties.getClaveTemporal()));
        }
        keycloakAdmin.asignarRolRealm(sub, rol);
        keycloakAdmin.prepararUsuarioParaPruebas(sub, keycloakProperties.getClaveTemporal());
        return sub;
    }

    private void asignar(Docente docente, ComisionLectiva comisionLectiva) {
        if (!asignaciones.existsByDocenteIdAndComisionLectivaId(
                docente.getId(), comisionLectiva.getId())) {
            asignaciones.saveAndFlush(new AsignacionDocente(docente, comisionLectiva));
        }
    }

    private String token(String username, String password) {
        if (password == null || password.isBlank()) {
            throw new ServicioNoDisponibleException(
                    "Falta la contraseña configurada para el seed de Postman");
        }
        return keycloakTokens.obtenerToken(username, password);
    }
}
