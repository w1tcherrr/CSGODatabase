package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.SteamAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SteamAccountRepository extends JpaRepository<SteamAccount, Long> {
    @Query("select distinct s.id64 from SteamAccount s left join CSGOAccount c on c.id64 = s.id64 where c is NULL")
    List<String> findAllUnmappedIDs();

    @Query("select count(distinct s) from SteamAccount s left join CSGOAccount c on c.id64 = s.id64 where c is NULL")
    long unmappedCount();

    @Query("select count(s) > 0 from SteamAccount s where s.id64 = :id")
    boolean containsById64(@Param("id") String current);
}
