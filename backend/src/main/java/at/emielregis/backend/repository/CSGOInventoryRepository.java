package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CSGOInventoryRepository extends JpaRepository<CSGOInventory, Long> {
    @Query(
        "select inv.id from CSGOAccount acc join acc.csgoInventory inv"
    )
    Set<Long> getNormalInvIDs();

    @Query(
        "select inv.id from CSGOInventory inv"
    )
    Set<Long> getAllInvIDs();
}
