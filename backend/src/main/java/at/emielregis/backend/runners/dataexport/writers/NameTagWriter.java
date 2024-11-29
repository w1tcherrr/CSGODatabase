package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NameTagWriter extends AbstractDataWriter {

    public NameTagWriter(ItemService itemService, SteamAccountService steamAccountService, ItemTypeService itemTypeService, CSGOAccountService csgoAccountService, CharmService charmService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Name Tag Data");

        SheetBuilder builder = SheetBuilder.create(workBook, "Name Tags");
        builder.setTitleRow("Name Tags");
        builder.setDescriptionRow("Name Tag", "Total Amount");

        Map<String, Integer> nameTagMap = itemService.getNameTagMap();

        List<Map.Entry<String, Integer>> sortedNameTagList = nameTagMap.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .toList();

        // Add rows to the sheet: Each row contains the name tag and the total amount
        sortedNameTagList.forEach(entry -> {
            String nameTag = entry.getKey();
            Integer totalAmount = entry.getValue();
            builder.addRow(null, nameTag, totalAmount.toString());
        });
    }
}
