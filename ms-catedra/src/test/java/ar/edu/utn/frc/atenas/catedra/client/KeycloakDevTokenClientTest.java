package ar.edu.utn.frc.atenas.catedra.client;

import ar.edu.utn.frc.atenas.commons.error.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class KeycloakDevTokenClientTest {

    private static final String TOKEN_URI = "http://localhost:8081/realms/atenas/protocol/openid-connect/token";

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    private MockRestServiceServer server;
    private KeycloakDevTokenClientImpl cliente;

    @BeforeEach
    void preparar() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("atenas-catedra-admin")
                .clientSecret("secreto-test")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(TOKEN_URI)
                .build();
        when(clientRegistrationRepository.findByRegistrationId("keycloak")).thenReturn(registration);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        cliente = new KeycloakDevTokenClientImpl(builder, clientRegistrationRepository);
    }

    @Test
    @DisplayName("Obtiene el access token con password grant")
    void deberiaObtenerToken_cuandoKeycloakResponde200() {
        server.expect(once(), requestTo(TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("username=jefe")))
                .andExpect(content().string(containsString("client_id=atenas-dev-tokens")))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token-jefe\",\"expires_in\":300}",
                        MediaType.APPLICATION_JSON));

        String token = cliente.obtenerToken("jefe", "clave");

        assertEquals("token-jefe", token);
        server.verify();
    }

    @Test
    @DisplayName("Traduce credenciales inválidas a servicio no disponible")
    void deberiaLanzarServicioNoDisponible_cuandoKeycloakRechaza() {
        server.expect(once(), requestTo(TOKEN_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(ServicioNoDisponibleException.class,
                () -> cliente.obtenerToken("jefe", "incorrecta"));
        server.verify();
    }
}
