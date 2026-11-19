package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDrive;

/**
 * Adaptador hacia Google Drive API v3. El service depende de esta interfaz para
 * poder verificar archivos sin acoplarse a la API externa.
 */
public interface DriveClient {

    /**
     * Extrae el fileId de un enlace habitual de Drive/Docs. Si no se reconoce,
     * lanza SolicitudInvalidaException (400).
     */
    String extraerFileId(String enlace);

    /**
     * Obtiene los metadatos del archivo. Lanza ArchivoNoAccesibleException si no
     * existe o es privado, y ServicioNoDisponibleException si Drive falla.
     */
    ArchivoDrive obtenerArchivo(String fileId);
}