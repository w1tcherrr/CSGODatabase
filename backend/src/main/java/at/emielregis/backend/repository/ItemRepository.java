package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemCollection;
import at.emielregis.backend.data.entities.items.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ItemRepository extends JpaRepository<ItemCollection, Long> {

    @Query(
        "SELECT sum(i.amount) from ItemCollection i"
    )
    long countTotalItems();

    @Modifying
    @Query(
        "Delete from ItemCollection i where i.id = :id"
    )
    void deleteById(@Param("id") Long id);

    @Query(
        "Select sum(i.amount) from ItemCollection i where i.itemType in :types"
    )
    Long sumForItemTypes(@Param("types") List<ItemType> itemType);

    @Query(
        "Select i.id from ItemCollection i where i not in (select i1 from CSGOInventory inv join inv.itemCollections i1)"
    )
    Set<Long> getOrphanedItemIds();

    @Query(
        "Select sum(i.amount) from ItemCollection i where i.itemType = :type"
    )
    int getTotalAmountForType(@Param("type") ItemType type);

    @Query("SELECT i.nameTag, SUM(i.amount) FROM ItemCollection i WHERE i.nameTag IS NOT NULL GROUP BY i.nameTag")
    List<Object[]> getNameTagCounts();

}
