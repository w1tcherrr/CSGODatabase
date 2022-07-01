package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.CSGOAccount;
import at.emielregis.backend.repository.CSGOAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CSGOAccountService {
    private final CSGOAccountRepository CSGOAccountRepository;

    public CSGOAccountService(CSGOAccountRepository CSGOAccountRepository) {
        this.CSGOAccountRepository = CSGOAccountRepository;
    }

    public void save(CSGOAccount account) {
        CSGOAccountRepository.save(account);
    }

    public boolean containsById64(String id64) {
        return CSGOAccountRepository.containsById64(id64);
    }

    public List<CSGOAccount> getAllWithInventory() {
        return CSGOAccountRepository.findAllWithInventory();
    }

    public long countWithInventory() {
        return CSGOAccountRepository.countWithInventory();
    }
}
