package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseWriter extends AbstractDataWriter {

    public CaseWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, CharmService charmService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ItemTypeService itemTypeService) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        List<ItemSet> caseSets = itemSetService.getAllCaseCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Item Name", "Total Amount (Cases)", "Total Amount (Skins)");

        List<String[]> overviewLines = new ArrayList<>();
        caseSets.forEach(set -> {
            List<ItemName> allValidItemNames = itemNameService.getAllNamesForSet(set);
            for (ItemName name : allValidItemNames) {
                if (name.getName().matches(".* Case ?[23]?")) {
                    overviewLines.add(new String[]{name.getName(), "" + itemService.getTotalAmountOfContainersForSet(set), "" + itemService.getTotalAmountOfNonContainersForSet(set)});
                }
            }
        });

        sortByNumericalColumn(overviewLines, 1);
        overviewLines.forEach(line -> overviewBuilder.addRow(null, line));

        for (ItemSet set : caseSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}
