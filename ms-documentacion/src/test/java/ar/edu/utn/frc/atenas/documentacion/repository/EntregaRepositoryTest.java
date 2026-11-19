package ar.edu.utn.frc.atenas.documentacion.repository;

import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EntregaRepositoryTest {

    @Autowired
    private EntregaRepository repository;

    @Test
    void uniqueHitoEquipo() {
        Entrega e1 = new Entrega();
        e1.setHitoId(1L);
        e1.setEquipoId(2L);
        e1.setEstado(Entrega.Estado.ENTREGADA);
        e1.setTardia(false);
        e1.setFechaEntrega(Instant.now());
        e1.setRegistradaPorSub("sub");
        repository.saveAndFlush(e1);

        Entrega e2 = new Entrega();
        e2.setHitoId(1L);
        e2.setEquipoId(2L);
        e2.setEstado(Entrega.Estado.ENTREGADA);
        e2.setTardia(false);
        e2.setFechaEntrega(Instant.now());
        e2.setRegistradaPorSub("sub");
        assertThatThrownBy(() -> repository.saveAndFlush(e2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}