package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.SteamGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link SteamGroup} entities.
 * Provides basic CRUD operations for Steam groups.
 */
public interface SteamGroupRepository extends JpaRepository<SteamGroup, Long> {
}
