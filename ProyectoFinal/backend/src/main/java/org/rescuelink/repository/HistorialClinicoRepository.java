package org.rescuelink.repository;

import org.rescuelink.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, UUID> {
    List<HistorialClinico> findByAnimalIdOrderByFechaRegistroDesc(UUID animalId);
}
