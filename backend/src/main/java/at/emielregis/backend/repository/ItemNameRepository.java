package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemNameRepository extends JpaRepository<ItemName, Long> {
    @Query(
        "Select i from ItemName i where upper(i.name) like UPPER(CONCAT('%',:filter,'%'))"
    )
    List<ItemName> getSearch(@Param("filter") String filter);

    ItemName findByName(String name);

    @Query(
        "Select count(i) > 0 from ItemType i where i.itemName = :name and i.exterior IS NOT NULL"
    )
    boolean itemNameHasExteriors(@Param("name") ItemName itemName);

    @Query(
        "Select distinct i.itemName from ItemType i where i.itemSet = :set"
    )
    List<ItemName> getAllNamesForSet(@Param("set") ItemSet set);
}
