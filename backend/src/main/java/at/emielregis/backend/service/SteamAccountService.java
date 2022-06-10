package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.repository.SteamAccountRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SteamAccountService {
    private final SteamAccountRepository steamAccountRepository;

    public SteamAccountService(SteamAccountRepository steamAccountRepository) {
        this.steamAccountRepository = steamAccountRepository;
    }

    public long count() {
        return steamAccountRepository.count();
    }

    public List<String> findNextIds(long l) {
        var ids = steamAccountRepository.findNextIds();
        Collections.shuffle(ids);
        return ids.stream().limit(l).collect(Collectors.toList());
    }

    public SteamAccount save(SteamAccount account) {
        if (account.getFriendIds() != null) {
            List<String> allFriendIds = steamAccountRepository.getAllUniqueFriendIDs();
            account.setFriendIds(account.getFriendIds().stream().filter(id -> !allFriendIds.contains(id)).collect(Collectors.toList()));
        }
        return steamAccountRepository.save(account);
    }

    public boolean containsBy64Id(String id64) {
        return steamAccountRepository.containsById64(id64);
    }

    public long getMaxPossibleAccounts() {
        return steamAccountRepository.getAmountOfUniqueAccountIDs();
    }

    public List<SteamAccount> getAllWithInventory() {
        return steamAccountRepository.findAllWithInventory();
    }
}
