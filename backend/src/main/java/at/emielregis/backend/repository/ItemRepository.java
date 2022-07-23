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
        "SELECT count(distinct i.id) from CSGOInventory inv join inv.itemCollections i"
    )
    long normalItemCount();

    @Query(
        "SELECT sum(i.amount) from ItemCollection i"
    )
    long itemCountNoStorageUnits();

    @Query(
        "SELECT sum(i.storageUnitAmount * i.amount) from ItemCollection i where i.storageUnitAmount IS NOT NULL"
    )
    Long itemCountInStorageUnits();

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
        "Select count(i) from ItemCollection i where i.itemType = :type and (i.storageUnitAmount IS NULL or i.storageUnitAmount = 0)"
    )
    long countEmptyStorageUnits(@Param("type") ItemType storageUnitType);

    @Query(
        "Select i from ItemCollection i where i.itemType = :type and (i.storageUnitAmount IS NOT NULL AND i.storageUnitAmount > 0)"
    )
    List<ItemCollection> getAllNonEmptyStorageUnits(@Param("type") ItemType storageUnitType);

    @Query(
        "Select i.id from ItemCollection i where i not in (select i1 from CSGOInventory inv join inv.itemCollections i1)"
    )
    Set<Long> getOrphanedItemIds();

    @Query(
        "Select sum(i.amount) from ItemCollection i where i.itemType = :type"
    )
    int getTotalAmountForType(@Param("type") ItemType type);
}
