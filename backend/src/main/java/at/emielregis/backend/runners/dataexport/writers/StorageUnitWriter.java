package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemCategoryService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemSetService;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.StickerService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StorageUnitWriter extends AbstractDataWriter {

    public StorageUnitWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        SheetBuilder storageUnitSheetBuilder = SheetBuilder.create(workBook, "Overview");
        storageUnitSheetBuilder.setTitleRow("Overview");
        storageUnitSheetBuilder.setDescriptionRow("Storage Unit Name", "Amount of Units with name", "Total amount of items");

        storageUnitSheetBuilder.addRow(null, "Empty units", "" + itemService.countAmountOfEmptyStorageUnits());
        storageUnitSheetBuilder.emptyLines(1);

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());

        List<Item> allStorageUnits = itemService.getAllNonEmptyStorageUnits();
        List<String> nameTags = allStorageUnits.stream().map(Item::getNameTag).distinct().toList();

        AtomicInteger index = new AtomicInteger(-1);
        int size = nameTags.size();
        nameTags.parallelStream().forEach(tag -> {
                if (index.incrementAndGet() % 100 == 0) {
                    LOGGER.info("Mapped " + index.get() + " names of " + size);
                }
                String[] line = new String[3];

                List<Item> storageUnits = allStorageUnits.stream().filter(unit -> unit.getNameTag().equals(tag)).toList();
                line[0] = tag;
                line[1] = "" + storageUnits.stream().mapToInt(Item::getAmount).sum();
                line[2] = "" + storageUnits.stream().mapToInt(Item::getStorageUnitAmount).filter(Objects::nonNull).sum();

                // we don't care about empty units
                if (Integer.parseInt(line[2]) == 0) {
                    return;
                }

                lines.add(line);
            }
        );

        // first sort alphabetically and then by the total amount - due to it being stable amount of same size will be alphabetically sorted
        lines.sort(Comparator.comparing(v -> v[0]));
        Collections.reverse(lines);
        sortByColumn(lines, 2);

        lines.forEach(line -> storageUnitSheetBuilder.addRow(null, line));
    }
}
