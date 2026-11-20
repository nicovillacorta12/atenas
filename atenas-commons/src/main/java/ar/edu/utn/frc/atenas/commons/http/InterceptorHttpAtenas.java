package ar.edu.utn.frc.atenas.commons.http;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import ar.edu.utn.frc.atenas.commons.traza.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Propaga el JWT del usuario y el {@code X-Correlation-Id} en las llamadas
 * salientes y traduce las fallas de conexión o timeout a 503.
 */
public class InterceptorHttpAtenas implements ClientHttpRequestInterceptor {

    public static final String CABECERA_SIMULAR_FALLA = "X-Simular-Falla";

    private static final Logger log = LoggerFactory.getLogger(InterceptorHttpAtenas.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest solicitud,
                                        byte[] cuerpo,
                                        ClientHttpRequestExecution ejecucion) throws IOException {
        String servicioSimulado = cabeceraDelRequest(CABECERA_SIMULAR_FALLA);
        String hostDestino = solicitud.getURI().getHost();
        if (servicioSimulado != null && hostDestino != null
                && servicioSimulado.equalsIgnoreCase(hostDestino)) {
            // Se usa la misma excepción que produciría una conexión caída para
            // ejercitar los flujos reales de 503 y degradación.
            log.warn("Falla simulada para el servicio {}", servicioSimulado);
            throw new SocketTimeoutException("Falla simulada de " + servicioSimulado);
        }

        String token = tokenDelUsuario();
        if (token != null && !token.isBlank()) {
            solicitud.getHeaders().setBearerAuth(token);
        }

        String correlationId = correlationDelRequest();
        if (correlationId != null && !correlationId.isBlank()) {
            solicitud.getHeaders().set(CorrelationIdFilter.CABECERA, correlationId);
        }

        try {
            return ejecucion.execute(solicitud, cuerpo);
        } catch (IOException excepcion) {
            log.warn("Falla de conexión o timeout en una llamada saliente", excepcion);
            throw new ServicioNoDisponibleException("No se pudo conectar con el servicio consultado");
        } catch (RuntimeException excepcion) {
            if (esTimeout(excepcion)) {
                log.warn("Timeout en una llamada saliente", excepcion);
                throw new ServicioNoDisponibleException("El servicio consultado no respondió a tiempo");
            }
            throw excepcion;
        }
    }

    private String tokenDelUsuario() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion instanceof JwtAuthenticationToken tokenJwt) {
            return tokenJwt.getToken().getTokenValue();
        }
        return null;
    }

    private String correlationDelRequest() {
        String correlationId = MDC.get(CorrelationIdFilter.NOMBRE_MDC);
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }
        return cabeceraDelRequest(CorrelationIdFilter.CABECERA);
    }

    private String cabeceraDelRequest(String nombre) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            return atributos.getRequest().getHeader(nombre);
        }
        return null;
    }

    private boolean esTimeout(Throwable excepcion) {
        Throwable causa = excepcion;
        while (causa != null) {
            if (causa instanceof SocketTimeoutException || causa instanceof TimeoutException) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }
}
