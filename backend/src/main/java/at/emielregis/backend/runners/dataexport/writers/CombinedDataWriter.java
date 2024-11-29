package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

@Component
public class CombinedDataWriter extends AbstractDataWriter {

    public CombinedDataWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, CharmService charmService, ItemTypeService itemTypeService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        SheetBuilder builder = SheetBuilder.create(workBook, "Combined Data");
        builder.setTitleRow("Combined Data");
        builder.setDescriptionRow("Item Name", "Total Count");
        createLinesForItemSearch("").forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
    }
}
