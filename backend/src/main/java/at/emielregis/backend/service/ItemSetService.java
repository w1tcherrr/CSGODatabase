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

@Component
public class ItemSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ItemSetRepository itemSetRepository;
    private final ResourceLoader resourceLoader;

    public ItemSetService(ItemSetRepository itemSetRepository, ResourceLoader resourceLoader) {
        this.itemSetRepository = itemSetRepository;
        this.resourceLoader = resourceLoader;
    }

    private List<String> readFile(String filename) {
        Resource resource = resourceLoader.getResource("classpath:collections/" + filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error reading file: " + filename, e);
            return Collections.emptyList();
        }
    }

    private List<ItemSet> searchByEquality(List<String> searchTerms) {
        List<ItemSet> sets = searchTerms.stream()
            .map(itemSetRepository::getByName)
            .filter(Objects::nonNull)
            .toList();
        return sets.stream().distinct().sorted(Comparator.comparing(ItemSet::getName)).collect(Collectors.toList());
    }

    private List<ItemSet> searchBySubstring(List<String> searchTerms) {
        List<ItemSet> sets = searchTerms.stream()
            .flatMap(search -> itemSetRepository.search(search).stream())
            .toList();
        return sets.stream().distinct().sorted(Comparator.comparing(ItemSet::getName)).collect(Collectors.toList());
    }

    public List<ItemSet> getAllCaseCollections() {
        LOGGER.info("ItemSetService#getAllCaseCollections()");
        return searchByEquality(readFile("case_collections.txt"));
    }

    public List<ItemSet> getAllSouvenirCollections() {
        LOGGER.info("ItemSetService#getAllSouvenirCollections()");
        return searchBySubstring(readFile("souvenir_collections.txt"))
            .stream()
            .filter(col -> !col.getName().contains("2021 Train")) // this is not a souvenir collection and was in the riptide shop
            .collect(Collectors.toList());
    }

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

    public List<ItemSet> getAllPatchCollections() {
        LOGGER.info("ItemSetService#getAllPatchCollections()");
        return searchByEquality(readFile("patch_collections.txt"));
    }

    public List<ItemSet> getAllGraffitiCollections() {
        LOGGER.info("ItemSetService#getAllGraffitiCollections()");
        return searchByEquality(readFile("graffiti_collections.txt"));
    }

    public List<ItemSet> getAllCharmCollections() {
        LOGGER.info("ItemSetService#getAllCharmCollections()");
        return searchByEquality(readFile("charm_collections.txt"));
    }

    public List<Exterior> getExteriorsForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#getExteriorsForItemSet(" + set.toString() + ")");
        return itemSetRepository.getExteriorsForSet(set);
    }

    public boolean hasStatTrakForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasStatTrakForItemSet(" + set.toString() + ")");
        return itemSetRepository.hasStatTrakForItemSet(set);
    }

    public boolean hasSouvenirForItemSet(ItemSet set) {
        LOGGER.info("ItemSetService#hasSouvenirForItemSet(" + set.toString() + ")");
        return itemSetRepository.hasSouvenirForItemSet(set);
    }

    public long count() {
        LOGGER.info("ItemSetService#count()");
        return itemSetRepository.count();
    }
}