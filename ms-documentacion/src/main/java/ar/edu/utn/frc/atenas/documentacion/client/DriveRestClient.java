package ar.edu.utn.frc.atenas.documentacion.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.http.AtenasHttpProperties;
import ar.edu.utn.frc.atenas.commons.http.TraductorErroresHttp;
import ar.edu.utn.frc.atenas.documentacion.config.DriveProperties;
import ar.edu.utn.frc.atenas.documentacion.dto.externo.ArchivoDriveDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementación de {@link DriveClient} sobre Google Drive API v3.
 *
 * Se construye un RestClient propio con los timeouts de commons, pero sin el
 * interceptor de JWT: ese token pertenece a Keycloak y no debe enviarse a
 * Google. La autenticación con Drive se hace mediante API key en la query.
 *
 * Cualquier falla se traduce a una excepción de commons para que D06 pueda
 * degradar el resultado a NO_VERIFICADO sin devolver 503.
 */
@Component
public class DriveRestClient implements DriveClient {

    private static final String SERVICIO = "Google Drive";

    private final RestClient restClient;
    private final DriveProperties propiedades;

    @Autowired
    public DriveRestClient(AtenasHttpProperties httpProperties, DriveProperties propiedades) {
        this(construirCliente(httpProperties, propiedades), propiedades);
    }

    /** Constructor para tests: permite inyectar un cliente contra un servidor simulado. */
    DriveRestClient(RestClient restClient, DriveProperties propiedades) {
        this.restClient = restClient;
        this.propiedades = propiedades;
    }

    @Override
    public ArchivoDrive obtenerArchivo(String driveFileId) {
        try {
            ArchivoDriveDto respuesta = restClient.get()
                    .uri(uri -> uri.path("/files/{id}")
                            .queryParam("fields", "id,name,mimeType,modifiedTime")
                            .queryParam("key", propiedades.getApiKey())
                            .build(driveFileId))
                    .retrieve()
                    .body(ArchivoDriveDto.class);

            if (respuesta == null || respuesta.modifiedTime() == null) {
                throw new ServicioNoDisponibleException(
                        SERVICIO + " devolvió una respuesta incompleta");
            }

            return new ArchivoDrive(
                    respuesta.id(),
                    respuesta.name(),
                    respuesta.mimeType(),
                    respuesta.modifiedTime());
        } catch (RestClientException excepcion) {
            throw new ServicioNoDisponibleException(
                    "No se pudo consultar el archivo en " + SERVICIO);
        }
    }

    private static RestClient construirCliente(AtenasHttpProperties httpProperties,
                                               DriveProperties propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Math.toIntExact(
                httpProperties.getTimeoutConexion().toMillis()));
        fabrica.setReadTimeout(Math.toIntExact(
                httpProperties.getTimeoutLectura().toMillis()));

        return RestClient.builder()
                .baseUrl(propiedades.getUrlBase())
                .requestFactory(fabrica)
                .defaultStatusHandler(HttpStatusCode::isError,
                        (solicitud, respuesta) -> TraductorErroresHttp.evaluar(respuesta))
                .build();
    }
}
