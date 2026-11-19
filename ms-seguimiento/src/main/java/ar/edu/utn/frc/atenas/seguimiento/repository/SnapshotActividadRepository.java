package ar.edu.utn.frc.atenas.seguimiento.repository;

import ar.edu.utn.frc.atenas.seguimiento.entity.SnapshotActividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnapshotActividadRepository extends JpaRepository<SnapshotActividad, Long> {

    /**
     * El snapshot más reciente de un repositorio para un sprint. Si dos tienen la
     * misma fecha de consulta, gana el de mayor id (el creado después).
     */
    Optional<SnapshotActividad> findFirstByRepositorioIdAndHitoIdOrderByFechaConsultaDescIdDesc(
            Long repositorioId, Long hitoId);
}
