package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PatchWriter extends AbstractDataWriter {

    public PatchWriter(ItemService itemService, SteamAccountService steamAccountService, ItemTypeService itemTypeService, CharmService charmService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Patch Collections");

        List<ItemSet> patchSets = itemSetService.getAllPatchCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Collection", "Total Amount (Containers)", "Total Amount (Patches)");

        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (ItemSet set : patchSets) {
            LOGGER.info("Currently mapping total amount of set " + current.incrementAndGet() + "/" + patchSets.size() + ": " + set.getName());
            rows.add(new String[]{set.getName(), "" + itemService.getTotalAmountOfContainersForSet(set), "" + itemService.getTotalAmountOfNonContainersForSet(set)});
        }

        sortByNumericalColumn(rows, 1);
        rows.forEach(line -> overviewBuilder.addRow(null, line));

        for (ItemSet set : patchSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}
