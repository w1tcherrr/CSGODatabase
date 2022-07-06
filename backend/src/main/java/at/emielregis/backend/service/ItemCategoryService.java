package at.emielregis.backend.service;

import at.emielregis.backend.repository.ItemCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public record ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public long count() {
        LOGGER.info("ItemCategoryService#count()");
        return itemCategoryRepository.count();
    }
}
