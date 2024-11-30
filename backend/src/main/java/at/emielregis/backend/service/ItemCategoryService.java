package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.items.ItemCategory;
import at.emielregis.backend.repository.ItemCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link ItemCategory} entities.
 * Provides methods to retrieve and count item categories, especially container-related categories.
 */
@Component
public record ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Counts the total number of item categories in the database.
     *
     * @return The total count of item categories.
     */
    public long count() {
        LOGGER.info("ItemCategoryService#count()");
        return itemCategoryRepository.count();
    }

    /**
     * Retrieves all container-related item categories from the database.
     * Filters out any null categories.
     *
     * @return A list of container item categories.
     */
    public List<ItemCategory> getAllContainerCategories() {
        LOGGER.info("ItemCategoryService#getAllContainerCategories()");
        List<ItemCategory> categories = new ArrayList<>();
        categories.add(itemCategoryRepository.findByName("Base Grade Container"));
        categories.add(itemCategoryRepository.findByName("Prototype Base Grade Container"));
        return categories.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
