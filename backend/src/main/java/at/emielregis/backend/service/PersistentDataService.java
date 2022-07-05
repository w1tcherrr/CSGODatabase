package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.PersistentDataStore;
import at.emielregis.backend.data.entities.SteamGroup;
import at.emielregis.backend.repository.PersistentDataRepository;
import at.emielregis.backend.repository.SteamGroupRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PersistentDataService {
    private final PersistentDataRepository persistentDataRepository;
    private final SteamGroupRepository steamGroupRepository;

    public PersistentDataService(PersistentDataRepository persistentDataRepository,
                                 SteamGroupRepository steamGroupRepository) {
        this.persistentDataRepository = persistentDataRepository;
        this.steamGroupRepository = steamGroupRepository;
    }

    public void save(PersistentDataStore store) {
        Long id = getInstanceId();
        if (!Objects.equals(store.getId(), id)) { // if the data store instance is not the same
            throw new IllegalArgumentException("Must not create more than one PersistentDataStore!");
        }
        persistentDataRepository.save(store);
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
                .steamGroups(new ArrayList<>())
                .build()
        );
    }

    private boolean noStore() {
        return persistentDataRepository.count() <= 0;
    }

    /**
     * Initializes the SteamGroup entities from the file.
     *
     * @param steamGroups The list of steam groups by their group name.
     * @return The list of the created steam group entities.
     */
    @Transactional
    public List<SteamGroup> initializeGroups(List<String> steamGroups) {
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        List<SteamGroup> toRemove = new ArrayList<>();

        // if a steam group is stored that is no longer specified in the file it is removed
        groups.forEach(k -> {
            if (!steamGroups.contains(k.getName())) {
                toRemove.add(k);
            }
        });
        groups.removeAll(toRemove);

        // add new entries for each group not stored in the datastore already
        steamGroups.forEach(group -> {
                if (!groups.contains(SteamGroup.builder().name(group).build())) {
                    SteamGroup group1 = SteamGroup.builder().name(group).mappedPages(new ArrayList<>()).build();
                    steamGroupRepository.save(group1);
                    groups.add(group1);
                }
            }
        );

        store.setSteamGroups(groups);
        save(store);

        // return only groups that are not locked - locked groups don't contain any more users and are not needed for further execution.
        return groups.stream().filter(group -> !group.isLocked()).collect(Collectors.toList());
    }

    /**
     * Gets the next unmapped page for the specified steam group.
     *
     * @param currentGroup The name of the group.
     * @return The unmapped page number.
     */
    @Transactional
    public synchronized long getNextPage(String currentGroup) {

        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getByName(groups, currentGroup);
        List<Integer> groupList = group.getMappedPages();

        // get the next unmapped page. Due to multi-threading the list is not always ascending without gaps.
        int index = 1;
        while (true) {
            if (!groupList.contains(index)) {
                groupList.add(index);
                group.setMappedPages(groupList);
                store.setSteamGroups(groups);
                save(store);
                return index;
            }
            ++index;
        }
    }

    /**
     * Frees the specified page - which means its mapping status is set to not mapped.
     * This is called whenever an exception in the REST call happens after the page was distributed to the proxy.
     *
     * @param currentGroup The name of the group.
     * @param currentPage The page to be removed.
     */
    @Transactional
    public synchronized void freePage(String currentGroup, long currentPage) {
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getByName(groups, currentGroup);
        List<Integer> groupList = group.getMappedPages();
        groupList.removeAll(List.of((int) currentPage)); // otherwise the index method is used
        group.setMappedPages(groupList);
        store.setSteamGroups(groups);
        save(store);
    }

    public SteamGroup getByName(List<SteamGroup> groups, String currentGroup) {
        for (SteamGroup group : groups) {
            if (group.getName().equals(currentGroup)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Group " + currentGroup + " does not exist");
    }

    /**
     * Locks a group, which means that requests for the groups members are no longer sent.
     *
     * @param currentGroup The name of the group to lock.
     */
    @Transactional
    public void lockGroup(String currentGroup) {
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getByName(groups, currentGroup);
        group.setLocked(true);
        save(store);
    }

    /**
     * Gets all unlocked groups. This should be called after locking a group to ensure the locked group is removed
     * from the current group selection.
     *
     * @return The list of groups.
     */
    @Transactional
    public List<SteamGroup> getUnlockedGroups() {
        PersistentDataStore store = getInstance();
        return store.getSteamGroups().stream().filter(s -> !s.isLocked()).collect(Collectors.toList());
    }
}
