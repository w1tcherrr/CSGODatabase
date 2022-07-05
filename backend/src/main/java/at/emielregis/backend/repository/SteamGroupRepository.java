package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.SteamGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SteamGroupRepository extends JpaRepository<SteamGroup, Long> {
}
