package at.emielregis.backend.runners;

import at.emielregis.backend.data.entities.CSGOInventory;
import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.SteamAccount;
import at.emielregis.backend.service.SteamAccountService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional
public class DataPrinter {
    private final SteamAccountService steamAccountService;

    public DataPrinter(SteamAccountService steamAccountService) {
        this.steamAccountService = steamAccountService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runDataFinder() {
        findContainers();
        findDragonLores();
    }

    public void findContainers() {
        Map<Item, Integer> combinedItemMap = getCombinedItemMap();

        List<Map.Entry<Item, Integer>> combinedContainerMap = new ArrayList<>(combinedItemMap.entrySet()
            .stream().filter(entry -> entry.getKey().getType().getName().equals("Base Grade Container"))
            .sorted(Comparator.comparingInt(Map.Entry::getValue)).toList());

        Collections.reverse(combinedContainerMap);

        System.out.println(" CONTAINERS ");
        for (Map.Entry<Item, Integer> entry : combinedContainerMap) {
            System.out.println(entry.getKey().getName() + " - " + entry.getValue());
        }
    }

    public void findDragonLores() {
        Map<Item, Integer> combinedItemMap = getCombinedItemMap();
        List<Map.Entry<Item, Integer>> combinedDragonMap = new ArrayList<>(combinedItemMap.entrySet()
            .stream().filter(entry -> entry.getKey().getName().toLowerCase().contains("dragon lore"))
            .sorted(Comparator.comparingInt(Map.Entry::getValue)).toList());

        Collections.reverse(combinedDragonMap);

        System.out.println(" DRAGON LORES ");
        for (Map.Entry<Item, Integer> entry : combinedDragonMap) {
            System.out.println(entry.getKey().getName() + " - " + entry.getValue());
        }
    }

    public Map<Item, Integer> getCombinedItemMap() {
        List<SteamAccount> accounts = steamAccountService.getAllWithInventory();
        List<CSGOInventory> inventories = accounts.stream().map(SteamAccount::getCsgoInventory).toList();
        List<Map<Item, Integer>> inventoryMaps = inventories.stream().map(CSGOInventory::getItems).filter(Objects::nonNull).toList();

        return inventoryMaps
            .stream().map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(Map.Entry::getKey))
            .entrySet().stream()
            .map((entry) -> Map.entry(entry.getKey(), entry.getValue().stream().mapToInt(Map.Entry::getValue).sum()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
