package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.repository.ItemSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link ItemSet} entities.
 * Provides methods to retrieve and analyze item sets and their properties.
 */
@Component
public class ItemSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ItemSetRepository itemSetRepository;
    private final ResourceLoader resourceLoader;

    public ItemSetService(ItemSetRepository itemSetRepository, ResourceLoader resourceLoader) {
        this.itemSetRepository = itemSetRepository;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Reads data from a file in the `collections` folder.
     *
     * @param filename The name of the file to read.
     * @return A list of strings from the file.
     */
    private List<String> readFile(String filename) {
        Resource resource = resourceLoader.getResource("classpath:collections/" + filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error reading file: " + filename, e);
            return Collections.emptyList();
        }
    }

    /**
     * Searches for item sets with exact matches for the given terms.
     *
     * @param searchTerms List of search terms.
     * @return List of matching {@link ItemSet}.
     */
    private List<ItemSet> searchByEquality(List<String> searchTerms) {
        return searchTerms.stream()
            .map(itemSetRepository::getByName)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.comparing(ItemSet::getName))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all case collections.
     *
     * @return List of all case collections.
     */
    public List<ItemSet> getAllCaseCollections() {
        LOGGER.info("ItemSetService#getAllCaseCollections()");
        return searchByEquality(readFile("case_collections.txt"));
    }

    /**
     * Retrieves all charm collections.
     *
     * @return List of all charm collections.
     */
    public List<ItemSet> getAllCharmCollections() {
        LOGGER.info("ItemSetService#getAllCharmCollections()");
        return searchByEquality(readFile("charm_collections.txt"));
    }

    /**
     * Retrieves all sticker collections.
     *
     * @return List of all sticker collections.
     */
    public List<ItemSet> getAllStickerCollections() {
        LOGGER.info("ItemSetService#getAllStickerCollections()");
        List<ItemSet> majorStickerCollections = searchByEquality(readFile("major_sticker_collections.txt"));
        List<ItemSet> otherStickerCollections = searchByEquality(readFile("other_sticker_collections.txt"));

        List<ItemSet> allStickerCollections = new ArrayList<>();
        allStickerCollections.addAll(majorStickerCollections);
        allStickerCollections.addAll(otherStickerCollections);

        return allStickerCollections.stream()
            .distinct()
            .sorted(Comparator.comparing(ItemSet::getName))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all exterior types for a specific item set.
     *
     * @param set The {@link ItemSet} to analyze.
     * @return List of {@link Exterior}.
     */
    public List<Exterior> getExteriorsForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#getExteriorsForItemSet(" + set + ")");
        return itemSetRepository.getExteriorsForSet(set);
    }

    /**
     * Determines if an item set has StatTrak items.
     *
     * @param set The {@link ItemSet} to analyze.
     * @return True if the set has StatTrak items, false otherwise.
     */
    public boolean hasStatTrakForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasStatTrakForItemSet(" + set + ")");
        return itemSetRepository.hasStatTrakForItemSet(set);
    }

    /**
     * Determines if an item set has Souvenir items.
     *
     * @param set The {@link ItemSet} to analyze.
     * @return True if the set has Souvenir items, false otherwise.
     */
    public boolean hasSouvenirForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasSouvenirForItemSet(" + set + ")");
        return itemSetRepository.hasSouvenirForItemSet(set);
    }

    /**
     * Counts all item sets in the repository.
     *
     * @return Total count of item sets.
     */
    public long count() {
        LOGGER.info("ItemSetService#count()");
        return itemSetRepository.count();
    }
}
