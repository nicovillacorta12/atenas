package ar.edu.utn.frc.atenas.catedra.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementación del password grant contra Keycloak para el seed de Postman.
 *
 * El cliente atenas-dev-tokens es público y solo existe para pruebas locales.
 * La contraseña viaja en el formulario y nunca se loguea.
 */
@Component
public class KeycloakDevTokenClientImpl implements KeycloakDevTokenClient {

    private static final String CLIENT_ID = "atenas-dev-tokens";

    private final RestClient restClient;
    private final String tokenUri;

    public KeycloakDevTokenClientImpl(
            @Qualifier("keycloakRestClientBuilder") RestClient.Builder restClientBuilder,
            ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("keycloak");
        if (registration == null) {
            throw new IllegalStateException("Falta la configuración del cliente Keycloak keycloak");
        }
        this.restClient = restClientBuilder.build();
        this.tokenUri = registration.getProviderDetails().getTokenUri();
    }

    @Override
    public String obtenerToken(String username, String password) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("grant_type", "password");
        formulario.add("client_id", CLIENT_ID);
        formulario.add("username", username);
        formulario.add("password", password);
        formulario.add("scope", "openid");

        try {
            RespuestaToken respuesta = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulario)
                    .retrieve()
                    .body(RespuestaToken.class);

            if (respuesta == null || respuesta.accessToken() == null || respuesta.accessToken().isBlank()) {
                throw new ServicioNoDisponibleException(
                        "Keycloak no devolvió un token para el usuario de prueba");
            }
            return respuesta.accessToken();
        } catch (RestClientException excepcion) {
            throw new ServicioNoDisponibleException(
                    "No se pudo obtener el token del usuario de prueba desde Keycloak");
        }
    }

    private record RespuestaToken(
            @JsonProperty("access_token") String accessToken) {
    }
}
