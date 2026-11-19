package ar.edu.utn.frc.atenas.documentacion.exception;

import ar.edu.utn.frc.atenas.commons.error.ErrorDominioException;

/**
 * El archivo en Drive no existe o no es accesible publicamente.
 */
import org.springframework.http.HttpStatus;

public class ArchivoNoAccesibleException extends ErrorDominioException {
    public ArchivoNoAccesibleException(String mensaje) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Archivo no accesible", mensaje);
    }
}