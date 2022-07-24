package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.runners.httpmapper.ItemPriceMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PriceWriter extends AbstractDataWriter {

    private final ItemPriceMapper itemPriceMapper;

    public PriceWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ItemTypeService itemTypeService, ItemPriceMapper itemPriceMapper) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
        this.itemPriceMapper = itemPriceMapper;
    }

    @Override
    protected void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Patch Collections");

        writeItemPrices(workBook);
        writeAppliedStickerPrices(workBook);
    }

    private void writeItemPrices(Workbook workBook) {
        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Items");
        overviewBuilder.setTitleRow("Item Prices");
        overviewBuilder.setDescriptionRow("Item Name", "Absolute Amount", "Average price per Item ($)", "Price in total ($)");

        AtomicReference<Double> totalPrice = new AtomicReference<>((double) 0);
        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        List<ItemName> names = itemNameService.getAll();
        names.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently mapping prices: " + current.incrementAndGet() + "/" + names.size() + ": " + itemName.getName());
            long totalAmount = itemService.getTotalAmountForName(itemName);
            Double totalPriceForItem = itemPriceMapper.getTotalPriceForName(itemName, totalAmount);
            if (totalPriceForItem != null) {
                totalPrice.updateAndGet(v -> v + totalPriceForItem);
                rows.add(new String[]{itemName.getName(), "" + totalAmount, "" + totalPriceForItem / totalAmount, "" + totalPriceForItem});
            } else {
                rows.add(new String[]{itemName.getName(), "" + totalAmount, "0", "0", "PRICE UNKNOWN"});
            }
        });

        overviewBuilder.emptyLines(1);
        overviewBuilder.addRow(null, "Total value in $: " + String.format(Locale.GERMAN, "%1$,.2f", totalPrice.get()));
        overviewBuilder.emptyLines(1);

        sortByNumericalColumn(rows, 3);
        rows.forEach(line -> overviewBuilder.addRow(null, line));
    }

    private void writeAppliedStickerPrices(Workbook workBook) {
        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Applied Stickers");
        overviewBuilder.setTitleRow("Applied Sticker Prices");
        overviewBuilder.setDescriptionRow("Sticker Name", "Absolute Amount (Only applied)", "Price per Item ($)", "Price in total ($)");

        AtomicReference<Double> totalPrice = new AtomicReference<>((double) 0);
        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        List<ItemName> names = itemNameService.getAll().stream().filter(name -> name.getName().startsWith("Sticker |")).toList();
        names.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently mapping sticker prices: " + current.incrementAndGet() + "/" + names.size() + ": " + itemName.getName());
            long totalAmount = stickerService.countTotalManuallyAppliedForItemName(itemName.getName());
            Double singleStickerItemPrice = itemPriceMapper.getSingleItemPriceForSticker(itemName);
            if (singleStickerItemPrice != null) {
                totalPrice.updateAndGet(v -> v + singleStickerItemPrice * totalAmount);
                rows.add(new String[]{itemName.getName(), "" + totalAmount, "" + singleStickerItemPrice, "" + singleStickerItemPrice * totalAmount});
            } else {
                rows.add(new String[]{itemName.getName(), "" + totalAmount, "0", "0", "PRICE UNKNOWN"});
            }
        });

        overviewBuilder.emptyLines(1);
        overviewBuilder.addRow(null, "Total value in $: " + String.format(Locale.GERMAN, "%1$,.2f", totalPrice.get()));
        overviewBuilder.emptyLines(1);

        sortByNumericalColumn(rows, 3);
        rows.forEach(line -> overviewBuilder.addRow(null, line));
    }
}
