package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MajorWriter extends AbstractDataWriter {

    private final ResourceLoader resourceLoader;

    public MajorWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, CharmService charmService, ItemTypeService itemTypeService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ResourceLoader resourceLoader) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
        this.resourceLoader = resourceLoader;
    }

    private List<String[]> readMajorCollections() {
        Resource resource = resourceLoader.getResource("classpath:collections/major_collections.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return br.lines().map(line -> line.split("\\|")).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error reading major collections file", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Major Data");

        List<String[]> majorList = readMajorCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Major", "Total Amount of Items", "Total Amount of Capsules");
        overviewBuilder.emptyLines(1);

        for (String[] strings : majorList) {
            LOGGER.info("Mapping total amounts for major {}", strings[0]);
            String[] currentRow = new String[3];
            currentRow[0] = strings[0];
            String[] stringCopy = Arrays.copyOfRange(strings, 1, strings.length);
            currentRow[1] = "" + itemService.getTotalAmountForNames(itemNameService.getSearch(stringCopy));
            currentRow[2] = "" + itemService.getTotalAmountOfContainersForNames(itemNameService.getSearch(stringCopy));
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
