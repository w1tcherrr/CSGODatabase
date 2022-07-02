package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.repository.SteamAccountRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SteamAccountService {
    private final SteamAccountRepository steamAccountRepository;
    private boolean initialized = false;
    private List<String> ids = new ArrayList<>();

    public SteamAccountService(SteamAccountRepository steamAccountRepository) {
        this.steamAccountRepository = steamAccountRepository;
    }

    public void init() {
        ids = steamAccountRepository.findAllUnmappedIDs();
    }

    public synchronized List<String> findNextIds(long l) {
        if (!initialized) {
            initialized = true;
            init();
        }
        if (l < 0) {
            return List.of();
        }
        List<String> result = ids.subList(0, (int) l);
        ids = ids.subList((int) l, ids.size());
        return result;
    }

    public long unmappedCount() {
        return steamAccountRepository.unmappedCount();
    }

    public long count() {
        return steamAccountRepository.count();
    }

    public boolean containsById64(String current) {
        return steamAccountRepository.containsById64(current);
    }

    public void saveAll(List<SteamAccount> accountList) {
        steamAccountRepository.saveAll(accountList);
    }
}
