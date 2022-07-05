package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.repository.ItemNameRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemNameService {
    private final ItemNameRepository itemNameRepository;

    public ItemNameService(ItemNameRepository itemNameRepository) {
        this.itemNameRepository = itemNameRepository;
    }

    public List<ItemName> getSearch(String filter) {
        return itemNameRepository.getSearch(filter);
    }
}
