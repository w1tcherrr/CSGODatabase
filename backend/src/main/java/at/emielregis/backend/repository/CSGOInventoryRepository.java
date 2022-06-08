package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.CSGOInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CSGOInventoryRepository extends JpaRepository<CSGOInventory, Long> {
}
