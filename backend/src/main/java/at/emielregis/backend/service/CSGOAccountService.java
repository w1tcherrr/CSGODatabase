package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.repository.CSGOAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class CSGOAccountService {
    private final CSGOAccountRepository CSGOAccountRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CSGOAccountService(CSGOAccountRepository CSGOAccountRepository) {
        this.CSGOAccountRepository = CSGOAccountRepository;
    }

    public void save(CSGOAccount account) {
        CSGOAccountRepository.save(account);
    }

    public boolean containsById64(String id64) {
        return CSGOAccountRepository.containsById64(id64);
    }

    public long countWithInventory() {
        LOGGER.info("CSGOAccountService#countWithInventory()");
        return CSGOAccountRepository.countWithInventory();
    }

    public Long count() {
        LOGGER.info("CSGOAccountService#count()");
        return CSGOAccountRepository.count();
    }
}
