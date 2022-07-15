package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemCategory;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        "SELECT count(distinct i.id) from CSGOInventory inv join inv.items i"
    )
    long normalItemCount();

    @Query(
        "SELECT distinct i.id from Item i"
    )
    Set<Long> getAllItemIDs();

    @Query(
        "SELECT sum(i.amount) from Item i"
    )
    long itemCountNoStorageUnits();

    @Query(
        "SELECT sum(i.storageUnitAmount * i.amount) from Item i where i.storageUnitAmount IS NOT NULL"
    )
    long itemCountInStorageUnits();

    @Query(
        "SELECT distinct i.name from Item i where i.itemSet = :set"
    )
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);

    @Query(
        "SELECT sum(i.amount) from Item i where i.name = :name"
    )
    long getTotalAmountForName(@Param("name") ItemName itemName);

    @Query(
        "SELECT sum(i.amount) from Item i where i.name in :names"
    )
    long getTotalAmountForNames(@Param("names") List<ItemName> search);

    @Query(
        "SELECT sum(i.amount) from Item i where i.name = :name and (i.souvenir = TRUE OR i.statTrak = TRUE)"
    )
    Long getSouvenirOrStatTrakAmountForName(@Param("name") ItemName itemName);

    @Query(
        "SELECT count(i) > 0 from Item i where i.exterior IS NOT NULL and i.name = :name"
    )
    boolean itemNameHasExteriors(@Param("name") ItemName itemName);

    @Query(
        "SELECT sum(i.amount) from Item i where i.exterior = :exterior and i.name = :name and i.souvenir = :souvenir and i.statTrak = :statTrak"
    )
    Long countForExteriorAndType(@Param("name") ItemName itemName, @Param("exterior") Exterior exterior, @Param("statTrak") boolean statTrak, @Param("souvenir") boolean souvenir);

    @Query(
        "Select sum(i.amount) from Item i where i.itemSet = :set and i.category not in :containerCategories"
    )
    Long countForSetNoContainers(@Param("set") ItemSet set, @Param("containerCategories") List<ItemCategory> containerCategories);

    @Query(
        "Select sum(i.amount) from Item i where i.itemSet = :set and i.category in :containerCategories"
    )
    Long countContainersForSet(@Param("set") ItemSet set, @Param("containerCategories") List<ItemCategory> containerCategories);

    @Modifying
    @Query(
        "Delete from Item i where i.id = :id"
    )
    void deleteById(@Param("id") Long id);

    @Query(
        "Select count (i) from Item i where i.name.name = 'Storage Unit' and (i.nameTag IS NULL OR i.storageUnitAmount IS NULL OR i.storageUnitAmount = 0)"
    )
    long getTotalAmountOfStorageUnitsWithNoName();

    @Query(
        "Select i from Item i where i.name.name = 'Storage Unit' and i.nameTag is NOT NULL and i.storageUnitAmount is NOT NULL and i.storageUnitAmount > 0"
    )
    List<Item> getAllNonEmptyStorageUnits();
}
