package at.emielregis.backend.runners.dataexport.writers;

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

import java.util.Arrays;
import java.util.List;

@Component
public class MajorWriter extends AbstractDataWriter {

    public MajorWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService) {
        super(itemService, steamAccountService, csgoAccountService, stickerService, itemSetService, itemNameService, itemCategoryService);
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Major Data");
        List<String[]> majorList = List.of(
            new String[]{"Antwerp 2022", "Antwerp 2022"},
            new String[]{"Stockholm 2021", "Stockholm 2021"},
            new String[]{"RMR 2020", "2020 RMR"},
            new String[]{"Berlin 2019", "Berlin 2019"},
            new String[]{"Katowice 2019", "Katowice 2019"},
            new String[]{"London 2018", "London 2018"},
            new String[]{"Boston 2018", "Boston 2018"},
            new String[]{"Krakow 2017", "Krakow 2017"},
            new String[]{"Atlanta 2017", "Atlanta 2017"},
            new String[]{"Cologne 2016", "Cologne 2016"},
            new String[]{"Columbus 2016", "Columbus 2016"},
            new String[]{"Cluj-Napoca 2015", "Cluj-Napoca 2015"},
            new String[]{"Cologne 2015", "Cologne 2015"},
            new String[]{"Katowice 2015", "Katowice 2015"},
            new String[]{"DreamHack Winter 2014", "DreamHack Winter 2014", "DreamHack 2014"},
            new String[]{"Cologne 2014", "Cologne 2014"},
            new String[]{"Katowice 2014", "Katowice 2014"},
            new String[]{"DreamHack Winter 2013", "DreamHack 2013", "DreamHack Winter 2013"}
        );

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Major", "Total Amount of Items from all collections (excluding storage units!)");
        overviewBuilder.emptyLines(1);

        for (String[] strings : majorList) {
            LOGGER.info("Mapping total amount for major {}", strings[0]);
            String[] currentRow = new String[2];
            currentRow[0] = strings[0];
            String[] stringCopy = Arrays.copyOfRange(strings, 1, strings.length);
            currentRow[1] = "" + itemService.getTotalAmountForNames(itemNameService.getSearch(stringCopy));
            overviewBuilder.addRow(null, currentRow);
        }

        for (String[] strings : majorList) {
            SheetBuilder builder = SheetBuilder.create(workBook, strings[0]);
            String[] searches = Arrays.copyOfRange(strings, 1, strings.length);
            builder.setTitleRow(strings[0]);
            builder.setDescriptionRow("Item Name", "Total Amount");
            createLinesForItemSearch(searches).forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}
