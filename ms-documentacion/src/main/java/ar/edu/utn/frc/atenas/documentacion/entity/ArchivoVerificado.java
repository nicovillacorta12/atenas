package ar.edu.utn.frc.atenas.documentacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Archivo verificado en Google Drive asociado a una entrega.
 */
@Entity
@Table(name = "archivo_verificado")
public class ArchivoVerificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entrega_id", nullable = false, unique = true)
    private Long entregaId;

    @Column(name = "enlace", nullable = false)
    private String enlace;

    @Column(name = "drive_file_id", nullable = false)
    private String driveFileId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "modified_time_entrega", nullable = false)
    private Instant modifiedTimeEntrega;

    public ArchivoVerificado() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public String getDriveFileId() { return driveFileId; }
    public void setDriveFileId(String driveFileId) { this.driveFileId = driveFileId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Instant getModifiedTimeEntrega() { return modifiedTimeEntrega; }
    public void setModifiedTimeEntrega(Instant modifiedTimeEntrega) { this.modifiedTimeEntrega = modifiedTimeEntrega; }
}