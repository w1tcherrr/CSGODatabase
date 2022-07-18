package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemCategoryService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemSetService;
import at.emielregis.backend.service.ItemTypeService;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.StickerService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

@Component
public class CombinedDataWriter extends AbstractDataWriter {

    public CombinedDataWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, ItemTypeService itemTypeService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        SheetBuilder builder = SheetBuilder.create(workBook, "Combined Data");
        builder.setTitleRow("Combined Data");
        builder.setDescriptionRow("Item Name", "Total Count");
        createLinesForItemSearch("").forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
    }
}
