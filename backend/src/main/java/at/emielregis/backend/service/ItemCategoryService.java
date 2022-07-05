package at.emielregis.backend.service;

import at.emielregis.backend.repository.ItemCategoryRepository;
import org.springframework.stereotype.Component;

@Component
public record ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
    public long count() {
        return itemCategoryRepository.count();
    }
}
