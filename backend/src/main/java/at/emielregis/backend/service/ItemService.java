package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.repository.ItemRepository;
import at.emielregis.backend.repository.ItemTypeRepository;
import org.springframework.stereotype.Component;

@Component
public record ItemService(ItemRepository itemRepository,
                          ItemTypeRepository itemTypeRepository) {
}