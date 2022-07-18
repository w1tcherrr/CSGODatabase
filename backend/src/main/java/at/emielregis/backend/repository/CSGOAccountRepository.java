package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CSGOAccountRepository extends JpaRepository<CSGOAccount, Long> {
    @Query(
        value = "Select count(a) > 0 from CSGOAccount a where a.id64 = :id"
    )
    boolean containsById64(@Param(value = "id") String id);

    @Query(
        value = "Select count(a) from CSGOAccount a where a.csgoInventory IS NOT NULL"
    )
    long countWithInventory();
}
