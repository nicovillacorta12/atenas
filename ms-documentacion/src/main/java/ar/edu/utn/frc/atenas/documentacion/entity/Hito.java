package ar.edu.utn.frc.atenas.documentacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Hito del curso (informe inicial, sprint o documento final). Lo define el jefe.
 * cursoLectivoId se guarda sin FK porque cruza esquemas (ms-catedra).
 */
@Entity
@Table(name = "hito")
public class Hito {

    public enum Tipo { INFORME_INICIAL, SPRINT, DOCUMENTO_FINAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curso_lectivo_id", nullable = false)
    private Long cursoLectivoId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private Tipo tipo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_limite", nullable = false)
    private Instant fechaLimite;

    public Hito() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCursoLectivoId() { return cursoLectivoId; }
    public void setCursoLectivoId(Long cursoLectivoId) { this.cursoLectivoId = cursoLectivoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Instant getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Instant fechaInicio) { this.fechaInicio = fechaInicio; }
    public Instant getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(Instant fechaLimite) { this.fechaLimite = fechaLimite; }
}