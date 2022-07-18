package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemSetRepository extends JpaRepository<ItemSet, Long> {
    ItemSet getByName(String name);

    @Query(
        "select i from ItemSet i where UPPER(i.name) like UPPER(concat('%',:search,'%'))"
    )
    List<ItemSet> search(@Param("search") String search);

    @Query(
        "Select distinct i.exterior from ItemType i where i.itemSet = :set and i.exterior IS NOT NULL"
    )
    List<Exterior> getExteriorsForSet(@Param("set") ItemSet set);

    @Query(
        "Select count(i) > 0 from ItemType i where i.itemSet = :set and i.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.STAT_TRAK"
    )
    boolean hasStatTrakForItemSet(@Param("set") ItemSet set);

    @Query(
        "Select count(i) > 0 from ItemType i where i.itemSet = :set and i.specialItemType = at.emielregis.backend.data.enums.SpecialItemType.SOUVENIR"
    )
    boolean hasSouvenirForItemSet(@Param("set") ItemSet set);

    ItemSet findByName(String name);
}
