package org.rescuelink.repository;

import org.rescuelink.model.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, UUID> {
    List<Donacion> findByAlbergueId(UUID albergueId);
}
