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
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

@Component
public class MiscellaneousDataWriter extends AbstractDataWriter {

    public MiscellaneousDataWriter(ItemService itemService, SteamAccountService steamAccountService, ItemTypeService itemTypeService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing miscellaneous Data");

        SheetBuilder builder = SheetBuilder.create(workBook, "Miscellaneous");
        builder.setTitleRow("Miscellaneous");

        // total amounts
        long noStorageUnitCount = itemService.countItemsNoStorageUnits();
        long onlyStorageUnitCount = itemService.countItemsInStorageUnits();
        long totalCount = noStorageUnitCount + onlyStorageUnitCount;

        builder.emptyLines(1);
        addRows(
            builder,
            "Explanation:",
            "Below you will find a short summary of some of the most important data surrounding this project.",
            "I queried a total of " + csgoAccountService.count() + " accounts, of which " + csgoAccountService.countWithInventory() + " had a public CSGO inventory.",
            "All accounts are taken from big public steam groups, such as 'Fnatic', 'hentaii' and 'esl'. I used groups from many countries and communities, ",
            "such as Brazil, China, Germany, USA, Denmark, Sweden, Argentina, Mongolia, Australia, etc. Obviously the biggest Steam Groups regarding CSGO are not ",
            "country-specific. Generally speaking, most accounts in this sample set should be quite representative of the wider mass of the CS:GO player-base.",
            "",
            "All accounts with fewer than 10 items are filtered out of the sample set to remove almost unused accounts. Due to the way the program works accounts",
            "with a VAC-Ban (or other game ban) or generally abandoned (offline for very long, no account activity for some time) are NOT filtered out. This is unfortunately",
            "not possible without incurring a massive penalty in processing speed.",
            "",
            "In the current state, the program can map around 12.500 accounts per hour using 100 proxy servers to spread the load over a plethora of IP-addresses.",
            "If no spreading is done the Steam API will limit the calls significantly and the mapping is far slower.",
            "",
            "Due to limitations in the API for public inventory access some properties of items can not be retrieved. For example, the float of a skin is not easily retrievable",
            "aswell as the 'state' of an item. As an example: Whether a Service Medal is green, blue, etc. is not sent via the API. Therefore I can't distinguish between the different levels for such items.",
            "Furthermore, some items are 'bugged' in the API. Generally, each gun/sticker/etc. belongs to a specific collection,",
            "such as 'The Recoil Collection' or similar. For some items this attribute is not sent via the API and therefore they can't be assigned to a specific collection.",
            "Furthermore, the content of storage units can not be retrieved. Only the name-tag and the amount of items in the unit is retrievable."
        );
        builder.emptyLines(1);

        builder.addRow(null, "Total Steam-Accounts found:", "" + steamAccountService.count());
        builder.addRow(null, "Total Steam-Accounts queried:", "" + csgoAccountService.count());
        builder.addRow(null, "Total CSGO-Accounts with inventories queried:", "" + csgoAccountService.countWithInventory());
        builder.emptyLines(1);

        builder.addRow(null, "Total Items (no Storage Units):", "" + noStorageUnitCount);
        builder.addRow(null, "Total Items in Storage Units:", "" + onlyStorageUnitCount);
        builder.addRow(null, "Total Items:", "" + totalCount);
        builder.emptyLines(1);

        builder.addRow(null, "Most Storage Units in one inventory:", "" + itemService.getHighestStorageUnitCount());
        builder.addRow(null, "Most full Storage Units in one inventory:", "" + itemService.getHighestFullStorageUnitCount());
        builder.addRow(null, "Most items in one inventory:", "" + itemService.getHighestSingleInventoryCount());
        builder.addRow(null, "Average items per inventory:", "" + totalCount / csgoAccountService.countWithInventory());
        builder.emptyLines(1);

        builder.addRow(null, "Total amount of item sets:", "" + itemSetService.count());
        builder.addRow(null, "Total amount of item categories:", "" + itemCategoryService.count());
        builder.addRow(null, "Total amount of different items:", "" + itemNameService.count());
        builder.addRow(null, "Total amount of different non-applied stickers:", "" + stickerService.countDistinctNonApplied());
        builder.addRow(null, "Total amount of different applied stickers:", "" + stickerService.countTypes(), "<- Note: This number is higher due to old unobtainable Souvenir stickers");
        builder.emptyLines(1);

        builder.addRow(null, "Total applied stickers:", "" + stickerService.countDistinctApplied());
    }

    private void addRows(SheetBuilder sheetBuilder, String... lines) {
        for (String line : lines) {
            sheetBuilder.addRow(null, line);
        }
    }
}
