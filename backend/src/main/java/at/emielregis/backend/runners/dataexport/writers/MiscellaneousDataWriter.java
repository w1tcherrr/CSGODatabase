package at.emielregis.backend.runners.dataexport.writers;

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

@Component
public class MiscellaneousDataWriter extends AbstractDataWriter {

    public MiscellaneousDataWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing miscellaneous Data");

        SheetBuilder builder = SheetBuilder.create(workBook, "Miscellaneous");
        builder.setTitleRow("Miscellaneous");

        // total amounts
        long noStorageUnitCount = itemService.itemCountNoStorageUnits();
        long onlyStorageUnitCount = itemService.itemCountOnlyStorageUnits();
        long totalCount = noStorageUnitCount + onlyStorageUnitCount;

        builder.emptyLines(1);

        builder.addRow(null, "Total Steam-Accounts queried:", "" + (steamAccountService.count()));
        builder.addRow(null, "Total CSGO-Accounts queried:", "" + (csgoAccountService.count()));
        builder.addRow(null, "Total CSGO-Accounts with inventories queried:", "" + (csgoAccountService.countWithInventory()));
        builder.emptyLines(1);

        builder.addRow(null, "Total Items (no Storage Units):", "" + (noStorageUnitCount));
        builder.addRow(null, "Total Items in Storage Units:", "" + (onlyStorageUnitCount));
        builder.addRow(null, "Total Items:", "" + (totalCount));
        builder.emptyLines(1);

        builder.addRow(null, "Most Storage Units in one inventory:", "" + (itemService.getHighestStorageUnitCount()));
        builder.addRow(null, "Most full Storage Units in one inventory:", "" + (itemService.getHighestFullStorageUnitCount()));
        builder.addRow(null, "Most items in one inventory:", "" + (itemService.getHighestSingleInventoryCount()));
        builder.addRow(null, "Least items in one inventory:", "" + (itemService.getLowestSingleInventoryCount()));
        builder.addRow(null, "Average items per inventory:", "" + (totalCount / csgoAccountService.countWithInventory()));
        builder.emptyLines(1);

        builder.addRow(null, "Total amount of item sets:", "" + (itemSetService.count()));
        builder.addRow(null, "Total amount of item categories:", "" + (itemCategoryService.count()));
        builder.addRow(null, "Total amount of different items:", "" + (itemNameService.count()));
        builder.addRow(null, "Total amount of different non-applied stickers:", "" + (stickerService.countDistinctNonApplied()));
        builder.addRow(null, "Total amount of different applied stickers:", "" + (stickerService.countTypes()), "<- Note: This number is higher due to old unobtainable Souvenir stickers");
        builder.emptyLines(1);

        builder.addRow(null, "Total applied stickers:", "" + (stickerService.countDistinctApplied()));
    }
}
