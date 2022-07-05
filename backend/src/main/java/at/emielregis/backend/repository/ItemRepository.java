package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
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
}
