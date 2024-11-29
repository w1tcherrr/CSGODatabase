package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writes charm data to an Excel workbook.
 * Generates sheets for each charm collection and an overview sheet.
 */
@Component
public class CharmWriter extends AbstractDataWriter {

    public CharmWriter(ItemService itemService,
                       SteamAccountService steamAccountService,
                       CSGOAccountService csgoAccountService,
                       CharmService charmService,
                       StickerService stickerService,
                       ItemTypeService itemTypeService,
                       ItemSetService itemSetService,
                       ItemNameService itemNameService,
                       ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Charm Data");

        List<ItemSet> charmSets = itemSetService.getAllCharmCollections();

        // Create overview sheet
        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Charm Collections Overview");
        overviewBuilder.setDescriptionRow("Collection Name", "Total Amount (Non-Applied)", "Total Amount (Applied)");

        List<String[]> overviewLines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger current = new AtomicInteger();

        charmSets.parallelStream().forEach(set -> {
            LOGGER.info("Processing charm collection {} of {}: {}", current.incrementAndGet(), charmSets.size(), set.getName());

            List<ItemName> charmItemNames = itemTypeService.getCharmItemNamesBySet(set);

            long totalNonApplied = 0;
            long totalApplied = 0;

            for (ItemName itemName : charmItemNames) {
                long nonApplied = itemService.getTotalAmountForName(itemName);
                long applied = charmService.countTotalAppliedForCharm(itemName.getName());
                totalNonApplied += nonApplied;
                totalApplied += applied;
            }

            overviewLines.add(new String[]{
                set.getName(),
                String.valueOf(totalNonApplied),
                String.valueOf(totalApplied)
            });
        });

        sortByNumericalColumn(overviewLines, 1);
        overviewLines.forEach(line -> overviewBuilder.addRow(null, line));

        for (ItemSet set : charmSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow("Charm Name", "Total Amount (Non-Applied)", "Total Amount (Applied)");

            List<ItemName> charmItemNames = itemTypeService.getCharmItemNamesBySet(set);
            List<String[]> lines = new ArrayList<>();

            for (ItemName itemName : charmItemNames) {
                long nonApplied = itemService.getTotalAmountForName(itemName);
                long applied = charmService.countTotalAppliedForCharm(itemName.getName());

                lines.add(new String[]{
                    itemName.getName(),
                    String.valueOf(nonApplied),
                    String.valueOf(applied)
                });
            }

            sortByNumericalColumn(lines, 1);
            lines.forEach(line -> builder.addRow(null, line));
        }

        // Process unclassified charms
        SheetBuilder unclassifiedBuilder = SheetBuilder.create(workBook, "Unclassified Charms");
        unclassifiedBuilder.setTitleRow("Unclassified Charms");
        unclassifiedBuilder.setDescriptionRow("Charm Name", "Total Amount (Non-Applied)", "Total Amount (Applied)");

        List<ItemName> unclassifiedCharmNames = itemTypeService.getUnclassifiedCharmItemNames();
        List<String[]> unclassifiedLines = new ArrayList<>();

        for (ItemName itemName : unclassifiedCharmNames) {
            long nonApplied = itemService.getTotalAmountForName(itemName);
            long applied = charmService.countTotalAppliedForCharm(itemName.getName());

            unclassifiedLines.add(new String[]{
                itemName.getName(),
                String.valueOf(nonApplied),
                String.valueOf(applied)
            });
        }

        sortByNumericalColumn(unclassifiedLines, 1);
        unclassifiedLines.forEach(line -> unclassifiedBuilder.addRow(null, line));
    }
}
