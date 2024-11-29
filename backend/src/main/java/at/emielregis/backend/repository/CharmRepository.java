package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.items.Charm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link Charm} entities.
 * Provides methods to perform CRUD operations and custom queries related to charms.
 */
public interface CharmRepository extends JpaRepository<Charm, Long> {

    /**
     * Counts the total number of times a charm with a specific name has been applied.
     *
     * @param name The name of the charm.
     * @return The count of applied charms with the given name.
     */
    @Query("SELECT COUNT(c) FROM ItemCollection i JOIN i.charm c WHERE c.name = :name")
    Long countTotalAppliedForItemName(@Param("name") String name);

    /**
     * Finds a charm entity by its name.
     *
     * @param name The name of the charm.
     * @return The charm entity if found, otherwise null.
     */
    Charm findByName(String name);
}
