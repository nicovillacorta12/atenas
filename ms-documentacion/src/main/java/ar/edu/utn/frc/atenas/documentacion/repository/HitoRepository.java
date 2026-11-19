package ar.edu.utn.frc.atenas.documentacion.repository;

import ar.edu.utn.frc.atenas.documentacion.entity.Hito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HitoRepository extends JpaRepository<Hito, Long> {
    List<Hito> findByCursoLectivoIdOrderByOrdenAsc(Long cursoLectivoId);
}