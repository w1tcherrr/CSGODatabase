package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link CSGOAccount} entities.
 * Provides methods for performing CRUD operations and custom queries related to CSGO accounts.
 */
public interface CSGOAccountRepository extends JpaRepository<CSGOAccount, Long> {

    /**
     * Checks if a CSGO account exists with the given Steam ID64.
     *
     * @param id The Steam ID64 of the account.
     * @return {@code true} if an account with the given ID exists, {@code false} otherwise.
     */
    @Query(
        value = "SELECT count(a) > 0 FROM CSGOAccount a WHERE a.id64 = :id"
    )
    boolean containsById64(@Param(value = "id") String id);

    /**
     * Counts the number of CSGO accounts that have a non-null inventory.
     *
     * @return The count of accounts with a non-null CSGO inventory.
     */
    @Query(
        value = "SELECT count(a) FROM CSGOAccount a WHERE a.csgoInventory IS NOT NULL"
    )
    long countWithInventory();
}
