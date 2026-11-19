package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.error.SolicitudInvalidaException;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDrive;
import ar.edu.utn.frc.atenas.documentacion.exception.ArchivoNoAccesibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementacion del cliente de Google Drive API v3 usando RestClient.
 */
@Component
public class DriveClientImpl implements DriveClient {

    private static final String BASE = "https://www.googleapis.com/drive/v3/files/";

    private static final Pattern[] PATRONES = new Pattern[] {
            Pattern.compile("drive\\.google\\.com/file/d/([^/]+)"),
            Pattern.compile("docs\\.google\\.com/document/d/([^/]+)"),
            Pattern.compile("open\\?id=([^&]+)")
    };

    private final RestClient restClient;
    private final String apiKey;

    public DriveClientImpl(RestClient.Builder builder,
                           @Value("${atenas.drive.api-key:}") String apiKey) {
        this.restClient = builder.build();
        this.apiKey = apiKey;
    }

    @Override
    public String extraerFileId(String enlace) {
        if (enlace == null || enlace.isBlank()) {
            throw new SolicitudInvalidaException("Enlace invalido");
        }
        for (Pattern p : PATRONES) {
            Matcher m = p.matcher(enlace);
            if (m.find()) {
                return m.group(1);
            }
        }
        throw new SolicitudInvalidaException("No se pudo reconocer el fileId del enlace");
    }

    @Override
    public ArchivoDrive obtenerArchivo(String fileId) {
        String url = BASE + fileId + "?fields=id,name,mimeType,modifiedTime&key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        try {
            RespuestaDrive respuesta = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        if (res.getStatusCode().value() == 404
                                || (res.getStatusCode().value() == 403
                                && (body.contains("notFound") || body.toLowerCase().contains("permission")))) {
                            throw new ArchivoNoAccesibleException("Archivo no accesible");
                        }
                        throw new ServicioNoDisponibleException("Drive no disponible");
                    })
                    .body(RespuestaDrive.class);

            if (respuesta == null) {
                throw new ServicioNoDisponibleException("Respuesta vacia de Drive");
            }
            Instant modified = Instant.parse(respuesta.modifiedTime());
            return new ArchivoDrive(respuesta.id(), respuesta.name(), respuesta.mimeType(), modified);
        } catch (ArchivoNoAccesibleException | ServicioNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioNoDisponibleException("Drive no disponible");
        }
    }

    private record RespuestaDrive(String id, String name, String mimeType, String modifiedTime) { }
}