package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
