package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.SteamAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SteamAccountRepository extends JpaRepository<SteamAccount, Long> {
    @Query(
        value = "Select count(a) > 0 from SteamAccount a where a.id64 = :id"
    )
    boolean containsBy64Id(@Param(value = "id") String id);

    @Query(
        value = "Select distinct id from " +
            "SteamAccount a join a.friendIds id " +
            "WHERE a.hasCsgo = TRUE AND a.privateFriends = FALSE " +
            "AND id NOT IN (SELECT distinct b.id64 from SteamAccount b)"
    )
    List<String> findNextIds();

    @Query(
        value = "Select count(distinct id) from SteamAccount a left join a.friendIds id"
    )
    long getMaxPossibleAccounts();

    @Query(
        value = "Select a from SteamAccount a where a.csgoInventory IS NOT NULL"
    )
    List<SteamAccount> findAllWithInventory();
}
