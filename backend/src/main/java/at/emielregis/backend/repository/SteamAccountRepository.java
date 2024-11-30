package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.SteamAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for {@link SteamAccount} entities.
 * Provides methods for managing and querying Steam accounts.
 */
public interface SteamAccountRepository extends JpaRepository<SteamAccount, Long> {

    /**
     * Finds all Steam account IDs that are not mapped to a CSGO account.
     *
     * @return A list of unmapped Steam account IDs.
     */
    @Query("SELECT DISTINCT s.id64 FROM SteamAccount s LEFT JOIN CSGOAccount c ON c.id64 = s.id64 WHERE c IS NULL")
    List<String> findAllUnmappedIDs();

    /**
     * Checks if a Steam account exists with the given Steam ID64.
     *
     * @param current The Steam ID64 to check.
     * @return {@code true} if the account exists, {@code false} otherwise.
     */
    @Query("SELECT count(s) > 0 FROM SteamAccount s WHERE s.id64 = :id")
    boolean containsById64(@Param("id") String current);
}
