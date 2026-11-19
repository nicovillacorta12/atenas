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
 * Entrega de un equipo para un hito. El estado PENDIENTE no se persiste: se
 * calcula combinando hitos con entregas existentes.
 */
@Entity
@Table(name = "entrega", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"hito_id", "equipo_id"}))
public class Entrega {

    public enum Estado { ENTREGADA, CORREGIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hito_id", nullable = false)
    private Long hitoId;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "tardia", nullable = false)
    private Boolean tardia;

    @Column(name = "fecha_entrega", nullable = false)
    private Instant fechaEntrega;

    @Column(name = "registrada_por_sub", nullable = false)
    private String registradaPorSub;

    @Column(name = "nota")
    private Integer nota;

    @Column(name = "fecha_correccion")
    private Instant fechaCorreccion;

    @Column(name = "corregida_por_sub")
    private String corregidaPorSub;

    @Column(name = "modificado_post_entrega")
    private String modificadoPostEntrega;

    public Entrega() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHitoId() { return hitoId; }
    public void setHitoId(Long hitoId) { this.hitoId = hitoId; }
    public Long getEquipoId() { return equipoId; }
    public void setEquipoId(Long equipoId) { this.equipoId = equipoId; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Boolean getTardia() { return tardia; }
    public void setTardia(Boolean tardia) { this.tardia = tardia; }
    public Instant getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Instant fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getRegistradaPorSub() { return registradaPorSub; }
    public void setRegistradaPorSub(String registradaPorSub) { this.registradaPorSub = registradaPorSub; }
    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }
    public Instant getFechaCorreccion() { return fechaCorreccion; }
    public void setFechaCorreccion(Instant fechaCorreccion) { this.fechaCorreccion = fechaCorreccion; }
    public String getCorregidaPorSub() { return corregidaPorSub; }
    public void setCorregidaPorSub(String corregidaPorSub) { this.corregidaPorSub = corregidaPorSub; }
    public String getModificadoPostEntrega() { return modificadoPostEntrega; }
    public void setModificadoPostEntrega(String modificadoPostEntrega) { this.modificadoPostEntrega = modificadoPostEntrega; }
}