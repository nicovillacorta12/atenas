package ar.edu.utn.frc.atenas.documentacion.repository;

import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/** Consultas de entregas para el cálculo de estados. */
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByHitoIdInAndEquipoIdIn(Collection<Long> hitoIds,
                                               Collection<Long> equipoIds);
}
