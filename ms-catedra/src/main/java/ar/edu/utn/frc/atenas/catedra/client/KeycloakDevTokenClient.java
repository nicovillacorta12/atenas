package ar.edu.utn.frc.atenas.catedra.client;

/**
 * Adaptador exclusivo para obtener tokens de usuarios de prueba.
 *
 * Usa el cliente público atenas-dev-tokens con Direct Access Grants. No se
 * usa para autenticar usuarios reales ni para operaciones de negocio.
 */
public interface KeycloakDevTokenClient {

    String obtenerToken(String username, String password);
}
