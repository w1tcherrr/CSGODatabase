package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(
        "SELECT distinct i.id from CSGOInventory inv join inv.items i"
    )
    Set<Long> getNormalItemIDs();

    @Query(
        "SELECT distinct i.id from Item i"
    )
    Set<Long> getAllItemIDs();

    @Query(
        "SELECT i from Item i where UPPER(i.name) LIKE UPPER(CONCAT('%',:search,'%'))"
    )
    List<Item> getSearch(@Param("search") String filter);

    @Query(
        "SELECT i from Item i where i.name = :name"
    )
    List<Item> getItemsForName(@Param("name") ItemName name);

    @Query(
        "SELECT sum(i.amount) from Item i"
    )
    long itemCountNoStorageUnits();

    @Query(
        "SELECT sum(i.storageUnitAmount * i.amount) from Item i where i.storageUnitAmount IS NOT NULL"
    )
    long itemCountOnlyStorageUnits();

    @Query(
        "SELECT distinct i.name from Item i where i.itemSet = :set"
    )
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);
}
