package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SouvenirWriter extends AbstractDataWriter {

    public SouvenirWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, ItemTypeService itemTypeService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        List<ItemSet> collectionSets = itemSetService.getAllSouvenirCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Collection", "Amount of Packages", "Amount of Skins");

        List<String[]> overviewLines = Collections.synchronizedList(new ArrayList<>());

        for (ItemSet set : collectionSets) {
            LOGGER.info("Mapping total amount for itemSet {}", set.getName());
            String[] currentRow = new String[3];
            currentRow[0] = set.getName();
            currentRow[1] = "" + itemService.getTotalAmountOfContainersForSet(set);
            currentRow[2] = "" + itemService.getTotalAmountOfNonContainersForSet(set);
            overviewLines.add(currentRow);
        }

        sortByNumericalColumn(overviewLines, 1);
        overviewLines.forEach(line -> overviewBuilder.addRow(null, line));

        for (ItemSet set : collectionSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}
