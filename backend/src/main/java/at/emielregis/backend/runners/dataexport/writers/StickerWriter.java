package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemCategoryService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemSetService;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.StickerService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StickerWriter extends AbstractDataWriter {

    public StickerWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Sticker Collections");

        List<ItemSet> stickerSets = itemSetService.getAllStickerCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Item Name", "Total Amount (Capsules)", "Total Amount (Stickers)", "Total Amount (Applied, Non-Souvenir Guns)", "Total Amount (Applied, Souvenir Guns)");

        List<String[]> rows = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger current = new AtomicInteger();
        AtomicInteger finalCurrent1 = current;
        List<String[]> finalRows1 = rows;
        stickerSets.parallelStream().forEach(set -> {
            LOGGER.info("Currently mapping total amounts of set " + finalCurrent1.incrementAndGet() + "/" + stickerSets.size() + ": " + set.getName());
            finalRows1.add(new String[]{
                set.getName(),
                "" + itemService.getTotalAmountOfContainersForSet(set),
                "" + itemService.getTotalAmountForSetNoContainers(set),
                "" + stickerService.countTotalManuallyAppliedForSet(set),
                "" + stickerService.countTotalSouvenirAppliedForSet(set)});
        });

        sortByColumn(rows, 1);
        rows.forEach(line -> overviewBuilder.addRow(null, line));
        overviewBuilder.emptyLines(1);
        overviewBuilder.addRow(null, "Note: A few old capsules are not identified by the API - their amount are not listed here.");

        SheetBuilder unclassifiedBuilder = SheetBuilder.create(workBook, "Unclassified");
        unclassifiedBuilder.setTitleRow("Unclassified Stickers");
        unclassifiedBuilder.setDescriptionRow("Item Name", "Total Amount (Non applied)", "Total Amount (Non-Souvenir Guns)", "Total Amount (Souvenir Guns)");

        List<ItemName> unclassifiedStickerNames = itemNameService.getUnclassifiedStickerNames();

        rows = Collections.synchronizedList(new ArrayList<>());
        List<String[]> finalRows = rows;
        current = new AtomicInteger();
        AtomicInteger finalCurrent = current;
        unclassifiedStickerNames.parallelStream().forEach(name -> {
            LOGGER.info("Currently mapping item " + finalCurrent.incrementAndGet() + "/" + unclassifiedStickerNames.size() + ": " + name.getName());
            String[] row = formatLineForItemName(name, false, false, List.of());
            row[2] = "" + stickerService.countTotalManuallyAppliedForItemName(name.getName());
            row[3] = "" + stickerService.countTotalSouvenirAppliedForItemName(name.getName());
            finalRows.add(row);
        });

        sortByColumn(rows, 1);
        rows.forEach(line -> unclassifiedBuilder.addRow(null, line));

        for (ItemSet set : stickerSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow("Item Name", "Total Amount (Non applied)", "Total Amount (Non-Souvenir Guns)", "Total Amount (Souvenir Guns)");
            lines = lines.subList(1, lines.size());
            lines.parallelStream().forEach(line -> line[2] = "" + stickerService.countTotalManuallyAppliedForItemName(line[0]));
            lines.parallelStream().forEach(line -> line[3] = "" + stickerService.countTotalSouvenirAppliedForItemName(line[0]));
            lines.forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}