package org.rescuelink.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.rescuelink.model.Animal;
import org.rescuelink.model.enums.EstadoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, UUID> {

    List<Animal> findByEstado(EstadoAnimal estado);

    /**
     * Bloqueo Pesimista Exclusivo (PESSIMISTIC_WRITE) para dictamen concurrente de adopciones.
     * Ejecuta 'SELECT ... FOR UPDATE' con timeout de 3 segundos para evitar dobles adopciones.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM Animal a WHERE a.id = :id")
    Optional<Animal> findByIdForUpdate(@Param("id") UUID id);
}
