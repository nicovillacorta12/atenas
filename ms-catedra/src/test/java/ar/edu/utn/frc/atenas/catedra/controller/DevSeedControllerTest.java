package ar.edu.utn.frc.atenas.catedra.controller;

import ar.edu.utn.frc.atenas.catedra.config.SecurityConfig;
import ar.edu.utn.frc.atenas.catedra.dto.DevSeedResponse;
import ar.edu.utn.frc.atenas.catedra.service.DevSeedService;
import ar.edu.utn.frc.atenas.commons.error.ManejadorGlobalDeErrores;
import ar.edu.utn.frc.atenas.commons.seguridad.ConversorJwtKeycloak;
import ar.edu.utn.frc.atenas.commons.seguridad.Roles;
import ar.edu.utn.frc.atenas.commons.tiempo.ZonaHoraria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DevSeedController.class)
@Import({SecurityConfig.class, ConversorJwtKeycloak.class, ManejadorGlobalDeErrores.class, ZonaHoraria.class})
class DevSeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DevSeedService devSeedService;

    @Test
    @DisplayName("Seed E1 sin token responde 401")
    void deberiaResponderNoAutorizado_cuandoNoHayToken() throws Exception {
        mockMvc.perform(post("/api/catedra/dev/seed-e1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Seed E1 con alumno responde 403")
    void deberiaResponderProhibido_cuandoNoEsJefe() throws Exception {
        mockMvc.perform(post("/api/catedra/dev/seed-e1")
                        .with(jwt().authorities(new SimpleGrantedAuthority(
                                Roles.PREFIJO_ROL + Roles.ALUMNO))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seed E1 con jefe devuelve IDs y tokens")
    void deberiaDevolverSeed_cuandoEsJefe() throws Exception {
        when(devSeedService.seedE1()).thenReturn(new DevSeedResponse(
                1L, 1L, 1L, 2L, 2L, 1L, 2L, 1L, 2L, 3L, "sub-rep",
                Map.of("jefe", "token-jefe", "docente-a", "token-docente")));

        mockMvc.perform(post("/api/catedra/dev/seed-e1")
                        .with(jwt().authorities(new SimpleGrantedAuthority(
                                Roles.PREFIJO_ROL + Roles.JEFE_CATEDRA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursoLectivoId").value(1))
                .andExpect(jsonPath("$.estudianteSub").value("sub-rep"))
                .andExpect(jsonPath("$.tokens.jefe").value("token-jefe"));
    }
}
