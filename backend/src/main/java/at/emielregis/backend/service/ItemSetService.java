package at.emielregis.backend.service;

import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.repository.ItemSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public record ItemSetService(ItemSetRepository itemSetRepository) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public List<ItemSet> searchBySubstring(String... searches) {
        List<ItemSet> sets = new ArrayList<>();
        for (String search : searches) {
            sets.addAll(itemSetRepository.search(search));
        }
        return sets.stream().distinct().toList();
    }

    public List<ItemSet> searchByEquality(String... searches) {
        List<ItemSet> sets = new ArrayList<>();
        for (String search : searches) {
            sets.add(itemSetRepository.getByName(search));
        }
        return sets.stream().distinct().toList();
    }

    public List<ItemSet> getAll() {
        return itemSetRepository.findAll().stream().sorted(Comparator.comparing(ItemSet::getName)).collect(Collectors.toList());
    }

    public List<Exterior> getExteriorsForItemSet(ItemSet set) {
        return itemSetRepository.getExteriorsForSet(set);
    }

    public boolean hasStatTrakForItemSet(ItemSet set) {
        return itemSetRepository.hasStatTrakForItemSet(set);
    }

    public boolean hasSouvenirForItemSet(ItemSet set) {
        return itemSetRepository.hasSouvenirForItemSet(set);
    }

    public long count() {
        LOGGER.info("ItemSetService#count()");
        return itemSetRepository.count();
    }
}
