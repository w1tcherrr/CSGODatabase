package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.PersistentDataStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository interface for {@link PersistentDataStore} entities.
 * Provides methods for managing and querying persistent data.
 */
public interface PersistentDataRepository extends JpaRepository<PersistentDataStore, Long> {

    /**
     * Retrieves the ID of the persistent data store.
     *
     * @return The ID of the persistent data store.
     */
    @Query("SELECT s.id FROM PersistentDataStore s")
    Long getId();
}
