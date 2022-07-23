package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.repository.CSGOInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Set;

@Component
public class CSGOInventoryService {
    private final CSGOInventoryRepository csgoInventoryRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CSGOInventoryService(CSGOInventoryRepository csgoInventoryRepository) {
        this.csgoInventoryRepository = csgoInventoryRepository;
    }

    public void save(CSGOInventory inventory) {
        LOGGER.info("CSGOInventoryService#save()");
        csgoInventoryRepository.saveAndFlush(inventory);
    }

    public long count() {
        LOGGER.info("CSGOInventoryService#count()");
        return csgoInventoryRepository.count();
    }

    @Transactional
    public void deleteAllById(Set<Long> orphanedIDs) {
        LOGGER.info("CSGOInventoryService#deleteAllById(" + orphanedIDs + ")");
        for (Long id : orphanedIDs) {
            csgoInventoryRepository.deleteById(id);
        }
        csgoInventoryRepository.flush();
    }

    public Set<Long> getAllInventoryIDs() {
        LOGGER.info("CSGOInventoryService#getAllInventoryIDs()");
        return csgoInventoryRepository.getAllInvIDs();
    }

    public Set<Long> getNormalInventoryIDs() {
        LOGGER.info("CSGOInventoryService#getNormalInventoryIDs()");
        return csgoInventoryRepository.getNormalInvIDs();
    }

    public long getNormalInventoryCount() {
        LOGGER.info("CSGOInventoryService#getNormalInventoryCount()");
        return csgoInventoryRepository.normalInventoryCount();
    }
}
