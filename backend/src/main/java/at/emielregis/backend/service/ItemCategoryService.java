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

@Component
public record ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public long count() {
        LOGGER.info("ItemCategoryService#count()");
        return itemCategoryRepository.count();
    }

    public List<ItemCategory> getAllContainerCategories() {
        LOGGER.info("ItemCategoryService#getAllContainerCategories()");
        List<ItemCategory> categories = new ArrayList<>();
        categories.add(itemCategoryRepository.findByName("Base Grade Container"));
        categories.add(itemCategoryRepository.findByName("Prototype Base Grade Container"));
        return categories.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
