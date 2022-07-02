package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.PersistentDataStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersistentDataRepository extends JpaRepository<PersistentDataStore, Long> {
    @Query(
        "select s.id from PersistentDataStore s"
    )
    Long getId();
}
