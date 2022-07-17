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
import java.util.List;

@Component
public class CaseWriter extends AbstractDataWriter {

    public CaseWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        List<ItemSet> caseSets = itemSetService.getAllCaseCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Item Name", "Total Amount (Cases)", "Total Amount (Skins)");

        List<String[]> overviewLines = new ArrayList<>();
        caseSets.parallelStream().forEach(set -> {
            List<ItemName> allValidItemNames = itemService.getAllNamesForSet(set);
            for (ItemName name : allValidItemNames) {
                if (name.getName().matches(".* Case ?[23]?")) {
                    overviewLines.add(new String[]{name.getName(), "" + itemService.getTotalAmountOfContainersForSet(set), "" + itemService.getTotalAmountForSetNoContainers(set)});
                }
            }
        });

        sortByColumn(overviewLines, 1);
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
