package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.repository.CSGOInventoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class CSGOInventoryService {
    private final CSGOInventoryRepository csgoInventoryRepository;

    public CSGOInventoryService(CSGOInventoryRepository csgoInventoryRepository) {
        this.csgoInventoryRepository = csgoInventoryRepository;
    }

    public void save(CSGOInventory inventory) {
        csgoInventoryRepository.save(inventory);
    }

    public long count() {
        return csgoInventoryRepository.count();
    }

    @Transactional
    public void deleteAllById(Set<Long> orphanedIDs) {
        for (Long id : orphanedIDs) {
            csgoInventoryRepository.deleteById(id);
        }
        csgoInventoryRepository.flush();
    }

    public Set<Long> getAllInventoryIDs() {
        return csgoInventoryRepository.getAllInvIDs();
    }

    public Set<Long> getNormalInventoryIDs() {
        return csgoInventoryRepository.getNormalInvIDs();
    }

    public long getNormalInventoryCount() {
        return csgoInventoryRepository.normalInventoryCount();
    }
}
