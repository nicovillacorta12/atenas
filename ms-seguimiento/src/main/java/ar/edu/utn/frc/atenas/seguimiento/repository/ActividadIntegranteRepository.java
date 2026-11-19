package ar.edu.utn.frc.atenas.seguimiento.repository;

import ar.edu.utn.frc.atenas.seguimiento.entity.ActividadIntegrante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadIntegranteRepository extends JpaRepository<ActividadIntegrante, Long> {

    List<ActividadIntegrante> findBySnapshotId(Long snapshotId);
}
