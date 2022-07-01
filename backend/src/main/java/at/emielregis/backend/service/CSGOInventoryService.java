package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.repository.CSGOInventoryRepository;
import org.springframework.stereotype.Component;

@Component
public class CSGOInventoryService {
    private final CSGOInventoryRepository csgoInventoryRepository;

    public CSGOInventoryService(CSGOInventoryRepository csgoInventoryRepository) {
        this.csgoInventoryRepository = csgoInventoryRepository;
    }

    public void save(CSGOInventory inventory) {
        csgoInventoryRepository.save(inventory);
    }
}
