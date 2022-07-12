package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface CSGOInventoryRepository extends JpaRepository<CSGOInventory, Long> {
    @Query(
        "select inv.id from CSGOAccount acc join acc.csgoInventory inv"
    )
    Set<Long> getNormalInvIDs();

    @Query(
        "select count(inv) from CSGOAccount acc join acc.csgoInventory inv"
    )
    long normalInventoryCount();

    @Query(
        "select inv.id from CSGOInventory inv"
    )
    Set<Long> getAllInvIDs();

    @Modifying
    @Query(
        "delete from CSGOInventory i where i.id = :id"
    )
    void deleteById(@Param("id") Long id);
}
