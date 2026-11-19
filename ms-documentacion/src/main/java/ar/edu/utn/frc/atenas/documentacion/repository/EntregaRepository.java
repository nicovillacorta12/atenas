package ar.edu.utn.frc.atenas.documentacion.repository;

import ar.edu.utn.frc.atenas.documentacion.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    Optional<Entrega> findByHitoIdAndEquipoId(Long hitoId, Long equipoId);
    List<Entrega> findByHitoIdInAndEquipoIdIn(List<Long> hitoIds, List<Long> equipoIds);
}