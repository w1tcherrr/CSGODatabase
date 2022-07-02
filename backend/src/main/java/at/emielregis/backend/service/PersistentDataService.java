package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.PersistentDataStore;
import at.emielregis.backend.repository.PersistentDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PersistentDataService {
    private final PersistentDataRepository persistentDataRepository;

    public PersistentDataService(PersistentDataRepository persistentDataRepository) {
        this.persistentDataRepository = persistentDataRepository;
    }

    public void save(PersistentDataStore store) {
        Long id = getInstanceId();
        if (!Objects.equals(store.getId(), id)) { // if the data store instance is not the same
            throw new IllegalArgumentException("Must not create more than one PersistentDataStore!");
        }
        persistentDataRepository.save(store);
    }

    @Transactional
    public Map<String, Integer> initializeGroups(List<String> steamGroups) {
        PersistentDataStore store = getInstance();
        Map<String, Integer> persistentMap = store.getSteamGroupPages();
        persistentMap.keySet().forEach(k -> {
            if (!steamGroups.contains(k)) {
                persistentMap.remove(k);
            }
        });
        steamGroups.forEach(group -> {
                if (!persistentMap.containsKey(group)) {
                    persistentMap.put(group, 1);
                }
            }
        );
        store.setSteamGroupPages(persistentMap);
        save(store);
        return persistentMap;
    }

    private PersistentDataStore getInstance() {
        if (noStore()) {
            createNewStore();
        }
        return persistentDataRepository.findAll().get(0);
    }

    private Long getInstanceId() {
        if (noStore()) {
            createNewStore();
        }
        return persistentDataRepository.getId();
    }

    private void createNewStore() {
        persistentDataRepository.save(
            PersistentDataStore
                .builder()
                .steamGroupPages(new HashMap<>())
                .build()
        );
    }

    private boolean noStore() {
        return persistentDataRepository.count() <= 0;
    }

    public void updateGroups(Map<String, Integer> groupMap) {
        PersistentDataStore instance = getInstance();
        instance.setSteamGroupPages(groupMap);
        save(instance);
    }
}
