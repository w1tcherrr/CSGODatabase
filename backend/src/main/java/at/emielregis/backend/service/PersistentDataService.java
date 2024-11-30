package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.PersistentDataStore;
import at.emielregis.backend.data.entities.SteamGroup;
import at.emielregis.backend.repository.PersistentDataRepository;
import at.emielregis.backend.repository.SteamGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service class for managing persistent data and Steam group mappings.
 * This class provides methods for handling the persistence and mapping of {@link SteamGroup} entities
 * as well as managing the {@link PersistentDataStore}.
 */
@Component
public class PersistentDataService {

    private final PersistentDataRepository persistentDataRepository;
    private final SteamGroupRepository steamGroupRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Constructor for {@link PersistentDataService}.
     *
     * @param persistentDataRepository Repository for accessing {@link PersistentDataStore}.
     * @param steamGroupRepository     Repository for managing {@link SteamGroup} entities.
     */
    public PersistentDataService(PersistentDataRepository persistentDataRepository,
                                 SteamGroupRepository steamGroupRepository) {
        this.persistentDataRepository = persistentDataRepository;
        this.steamGroupRepository = steamGroupRepository;
    }

    /**
     * Saves a {@link PersistentDataStore} instance to the database.
     *
     * @param store The {@link PersistentDataStore} to save.
     */
    public void save(PersistentDataStore store) {
        LOGGER.info("PersistentDataService#save()");
        Long id = getInstanceId();
        if (!Objects.equals(store.getId(), id)) {
            throw new IllegalArgumentException("Must not create more than one PersistentDataStore!");
        }
        persistentDataRepository.save(store);
    }

    /**
     * Retrieves the singleton instance of {@link PersistentDataStore}.
     * If no instance exists, a new one is created.
     *
     * @return The singleton instance of {@link PersistentDataStore}.
     */
    private PersistentDataStore getInstance() {
        LOGGER.info("PersistentDataService#getInstance()");
        if (noStoreExists()) {
            createNewStore();
        }
        return persistentDataRepository.findAll().get(0);
    }

    /**
     * Retrieves the ID of the current {@link PersistentDataStore} instance.
     * Creates a new instance if none exists.
     *
     * @return The ID of the {@link PersistentDataStore}.
     */
    private Long getInstanceId() {
        LOGGER.info("PersistentDataService#getInstanceId()");
        if (noStoreExists()) {
            createNewStore();
        }
        return persistentDataRepository.getId();
    }

    /**
     * Creates a new {@link PersistentDataStore} instance and saves it to the database.
     */
    private void createNewStore() {
        LOGGER.info("PersistentDataService#createNewStore()");
        persistentDataRepository.save(
            PersistentDataStore.builder()
                .steamGroups(new ArrayList<>())
                .build()
        );
    }

    /**
     * Checks if no {@link PersistentDataStore} exists in the database.
     *
     * @return True if no store exists, false otherwise.
     */
    private boolean noStoreExists() {
        LOGGER.info("PersistentDataService#noStoreExists()");
        return persistentDataRepository.count() <= 0;
    }

    /**
     * Initializes the Steam groups by creating entities for the provided group names.
     * Removes groups that are no longer specified in the input list.
     *
     * @param steamGroups The list of Steam group names to initialize.
     * @return The list of initialized Steam groups that are not locked.
     */
    @Transactional
    public List<SteamGroup> initializeGroups(List<String> steamGroups) {
        LOGGER.info("PersistentDataService#initializeGroups({})", steamGroups);
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        List<SteamGroup> toRemove = new ArrayList<>();

        groups.forEach(group -> {
            if (!steamGroups.contains(group.getName())) {
                toRemove.add(group);
            }
        });
        groups.removeAll(toRemove);

        steamGroups.forEach(groupName -> {
            if (!groups.contains(SteamGroup.builder().name(groupName).build())) {
                SteamGroup group = SteamGroup.builder().name(groupName).mappedPages(new ArrayList<>()).build();
                steamGroupRepository.save(group);
                groups.add(group);
            }
        });

        store.setSteamGroups(groups);
        save(store);

        return groups.stream().filter(group -> !group.isLocked()).collect(Collectors.toList());
    }

    /**
     * Retrieves the next unmapped page number for a given Steam group.
     *
     * @param currentGroup The name of the group.
     * @return The next unmapped page number.
     */
    @Transactional
    public synchronized long getNextPage(String currentGroup) {
        LOGGER.info("PersistentDataService#getNextPage({})", currentGroup);

        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getGroupByName(groups, currentGroup);
        List<Integer> mappedPages = group.getMappedPages();

        int index = 1;
        while (true) {
            if (!mappedPages.contains(index)) {
                mappedPages.add(index);
                group.setMappedPages(mappedPages);
                store.setSteamGroups(groups);
                save(store);
                return index;
            }
            ++index;
        }
    }

    /**
     * Frees a specified page for a given Steam group, marking it as unmapped.
     *
     * @param currentGroup The name of the group.
     * @param currentPage  The page to be freed.
     */
    @Transactional
    public synchronized void freePage(String currentGroup, long currentPage) {
        LOGGER.info("PersistentDataService#freePage({}, {})", currentGroup, currentPage);
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getGroupByName(groups, currentGroup);
        List<Integer> mappedPages = group.getMappedPages();
        mappedPages.removeAll(List.of((int) currentPage));
        group.setMappedPages(mappedPages);
        store.setSteamGroups(groups);
        save(store);
    }

    /**
     * Retrieves a Steam group by its name from a list of groups.
     *
     * @param groups       The list of groups.
     * @param currentGroup The name of the group to retrieve.
     * @return The {@link SteamGroup} with the specified name.
     * @throws IllegalArgumentException If the group does not exist.
     */
    public SteamGroup getGroupByName(List<SteamGroup> groups, String currentGroup) {
        LOGGER.info("PersistentDataService#getGroupByName({})", currentGroup);
        return groups.stream()
            .filter(group -> group.getName().equals(currentGroup))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Group " + currentGroup + " does not exist"));
    }

    /**
     * Locks a Steam group, marking it as unavailable for further mapping.
     *
     * @param currentGroup The name of the group to lock.
     */
    @Transactional
    public void lockGroup(String currentGroup) {
        LOGGER.info("PersistentDataService#lockGroup({})", currentGroup);
        PersistentDataStore store = getInstance();
        List<SteamGroup> groups = store.getSteamGroups();
        SteamGroup group = getGroupByName(groups, currentGroup);
        group.setLocked(true);
        save(store);
    }

    /**
     * Retrieves all unlocked Steam groups, which are eligible for further mapping.
     *
     * @return A list of unlocked {@link SteamGroup} entities.
     */
    @Transactional
    public List<SteamGroup> getUnlockedGroups() {
        LOGGER.info("PersistentDataService#getUnlockedGroups()");
        PersistentDataStore store = getInstance();
        return store.getSteamGroups().stream()
            .filter(group -> !group.isLocked())
            .collect(Collectors.toList());
    }
}
