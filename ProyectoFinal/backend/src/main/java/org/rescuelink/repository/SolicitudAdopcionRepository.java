package org.rescuelink.repository;

import org.rescuelink.model.SolicitudAdopcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, UUID> {
    Optional<SolicitudAdopcion> findByCodigoCertificadoQr(String codigoCertificadoQr);
    List<SolicitudAdopcion> findByAnimalId(UUID animalId);
    List<SolicitudAdopcion> findByAdoptanteId(UUID adoptanteId);
}
