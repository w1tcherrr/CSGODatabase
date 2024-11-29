package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemSet;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GraffitiWriter extends AbstractDataWriter {

    private final ResourceLoader resourceLoader;

    public GraffitiWriter(ItemService itemService, SteamAccountService steamAccountService, ItemTypeService itemTypeService, CharmService charmService, CSGOAccountService csgoAccountService, StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ResourceLoader resourceLoader) {
        super(itemService, steamAccountService, csgoAccountService, charmService, stickerService, itemSetService, itemNameService, itemCategoryService, itemTypeService);
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void writeWorkbook(Workbook workBook) {
        LOGGER.info("Writing Graffiti Collections");

        List<ItemSet> graffitiSets = itemSetService.getAllGraffitiCollections();

        List<String> majorsWithGraffities;
        Resource resource = resourceLoader.getResource("classpath:collections/graffiti_majors.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            majorsWithGraffities = br.lines().toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Collection", "Total Amount (Containers/Sealed)", "Total Amount (Graffities)");

        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (ItemSet set : graffitiSets) {
            LOGGER.info("Currently mapping total amount of set " + current.incrementAndGet() + "/" + graffitiSets.size() + ": " + set.getName());
            rows.add(new String[]{set.getName(), "" + itemService.getTotalAmountOfContainersForSet(set), "" + itemService.getTotalAmountOfNonContainersForSet(set)});
        }

        for (String major : majorsWithGraffities) {
            LOGGER.info("Currently mapping total amounts for major " + major);
            long totalAmount = itemService.getTotalAmountForNames(itemNameService.getSearch("Graffiti%" + major));
            long totalAmountContainers = itemService.getTotalAmountForNames(itemNameService.getSearch("Sealed Graffiti%" + major));
            rows.add(new String[]{major, "" + totalAmountContainers, "" + (totalAmount - totalAmountContainers)});
        }

        sortByNumericalColumn(rows, 1);
        rows.forEach(line -> overviewBuilder.addRow(null, line));

        for (ItemSet set : graffitiSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }

        for (String major : majorsWithGraffities) {
            SheetBuilder builder = SheetBuilder.create(workBook, major);
            List<String[]> lines = createLinesForItemSearch("Graffiti%" + major);
            builder.setTitleRow(major + " Graffities");
            builder.setDescriptionRow("Item Name", "Total Count");
            lines.forEach(line -> builder.addRow(new CellStyle[]{getStyleForName(workBook, line[0])}, line));
        }
    }
}
