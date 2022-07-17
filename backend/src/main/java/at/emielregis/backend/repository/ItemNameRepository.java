package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemNameRepository extends JpaRepository<ItemName, Long> {
    boolean existsByName(String name);

    ItemName getByName(String name);

    @Query(
        "Select i from ItemName i where upper(i.name) like UPPER(CONCAT('%',:filter,'%'))"
    )
    List<ItemName> getSearch(@Param("filter") String filter);

    @Query(
        "Select distinct i.name from Item i where i.name.name like '%Sticker%' and i.itemSet IS NULL"
    )
    List<ItemName> getUnclassifiedStickerNames();

    @Query(
        "Select distinct i.rarity from Item i where i.name.name = :name"
    )
    Rarity getRarityForItemNameName(@Param("name") String itemNameName);
}
