package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.data.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CSGOAccountRepository extends JpaRepository<CSGOAccount, Long> {
    @Query(
        value = "Select count(a) > 0 from CSGOAccount a where a.id64 = :id"
    )
    boolean containsById64(@Param(value = "id") String id);

    @Query(
        value = "Select a from CSGOAccount a where a.csgoInventory IS NOT NULL ORDER BY a.id"
    )
    List<CSGOAccount> findAllWithInventory();

    @Query(
        value = "Select count(a) from CSGOAccount a where a.csgoInventory IS NOT NULL"
    )
    long countWithInventory();

    @Query(
        value = "Select distinct a.id64 from CSGOAccount a WHERE a.csgoInventory IS NOT NULL"
    )
    List<String> getAllMappedIDs();

    @Query(
        value = "select distinct i from CSGOAccount a join a.csgoInventory inv join inv.items i where " +
            "i.itemSet.name like concat('%','Patch','%')"
    )
    List<Item> getItemsWithPatchSet();
}
