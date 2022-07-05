package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemSetService;
import org.apache.catalina.manager.JspHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Transactional
public class DataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOAccountService CSGOAccountService;
    private final ItemService itemService;
    private final ItemSetService itemSetService;
    private final ItemNameService itemNameService;

    public DataWriter(CSGOAccountService CSGOAccountService,
                      ItemService itemService,
                      ItemSetService itemSetService, ItemNameService itemNameService) {
        this.CSGOAccountService = CSGOAccountService;
        this.itemService = itemService;
        this.itemSetService = itemSetService;
        this.itemNameService = itemNameService;
    }

    public void write() {
        LOGGER.info("Writing Data now");

        // writes all raw data into a single file
        //writeAll();

        // writes some miscellaneous data
        writeMiscellaneous();

        // writes data for all majors
        writeMajors();

        // writes data for all normal item collections
        writeCollections();
    }

    private void writeAll() {
        LOGGER.info("Writing All Data");
        Workbook workBook = new XSSFWorkbook();
        createSheet(workBook, "Combined Data", createLinesForItemSearch(""));
        writeWorkBookToFile("Combined_Data.xlsx", workBook);
    }

    private void writeMiscellaneous() {
        LOGGER.info("Writing miscellaneous Data");
        Workbook workBook = new XSSFWorkbook();

        List<String[]> miscellaneousData = new ArrayList<>();

        // total amounts
        long noStorageUnitCount = itemService.itemCountNoStorageUnits();
        long onlyStorageUnitCount = itemService.itemCountOnlyStorageUnits();
        long totalCount = noStorageUnitCount + onlyStorageUnitCount;

        emptyLine(miscellaneousData);

        addLine(miscellaneousData, "Total Items (no Storage Units):", "" + formatNumber(noStorageUnitCount));
        addLine(miscellaneousData, "Total Items in Storage Units:", "" + formatNumber(onlyStorageUnitCount));
        addLine(miscellaneousData, "Total Items:", "" + formatNumber(totalCount));
        emptyLine(miscellaneousData);

        addLine(miscellaneousData, "Most Storage Units in one inventory:", "" + formatNumber(itemService.getHighestStorageUnitCount()));
        addLine(miscellaneousData, "Most full Storage Units in one inventory:", "" + formatNumber(itemService.getHighestFullStorageUnitCount()));
        addLine(miscellaneousData, "Most items in one inventory:", "" + formatNumber(itemService.getHighestSingleInventoryCount()));
        addLine(miscellaneousData, "Least items in one inventory:", "" + formatNumber(itemService.getLowestSingleInventoryCount()));
        addLine(miscellaneousData, "Average items per inventory:", "" + formatNumber(totalCount / CSGOAccountService.countWithInventory()));
        emptyLine(miscellaneousData);

        createSheet(workBook, "Miscellaneous", miscellaneousData);
        writeWorkBookToFile("Miscellaneous_Data.xlsx", workBook);
    }

    private void writeMajors() {
        LOGGER.info("Writing Major Data");
        Workbook workBook = new XSSFWorkbook();
        createSheet(workBook, "Antwerp 2022", createLinesForItemSearch("Antwerp 2022"));
        createSheet(workBook, "Stockholm 2021", createLinesForItemSearch("Stockholm 2021"));
        createSheet(workBook, "Berlin 2019", createLinesForItemSearch("Berlin 2019"));
        createSheet(workBook, "Katowice 2019", createLinesForItemSearch("Katowice 2019"));
        createSheet(workBook, "London 2018", createLinesForItemSearch("London 2018"));
        createSheet(workBook, "Boston 2018", createLinesForItemSearch("Boston 2018"));
        createSheet(workBook, "Krakow 2017", createLinesForItemSearch("Krakow 2017"));
        createSheet(workBook, "Atlanta 2017", createLinesForItemSearch("Atlanta 2017"));
        createSheet(workBook, "Cologne 2016", createLinesForItemSearch("Cologne 2016"));
        createSheet(workBook, "Columbus 2016", createLinesForItemSearch("Columbus 2016"));
        createSheet(workBook, "Cluj-Napoca 2015", createLinesForItemSearch("Cluj-Napoca 2015"));
        createSheet(workBook, "Cologne 2015", createLinesForItemSearch("Cologne 2015"));
        createSheet(workBook, "Katowice 2015", createLinesForItemSearch("Katowice 2015"));
        createSheet(workBook, "DreamHack Winter 2014", createLinesForItemSearch("DreamHack Winter 2014", "DreamHack 2014"));
        createSheet(workBook, "Cologne 2014", createLinesForItemSearch("Cologne 2014"));
        createSheet(workBook, "Katowice 2014", createLinesForItemSearch("Katowice 2014"));
        createSheet(workBook, "DreamHack Winter 2013", createLinesForItemSearch("DreamHack 2013", "DreamHack Winter 2013"));
        writeWorkBookToFile("All_Major_Items.xlsx", workBook);
    }

    private void writeCollections() {
        LOGGER.info("Writing Collection Data");
        writeAllCollections();
        writeSouvenirCollections();
    }

    private void writeAllCollections() {
        Workbook workBook = new XSSFWorkbook();
        List<ItemSet> collectionSets = itemSetService.getAll();
        collectionSets.forEach(set -> createSheet(workBook, set.getName(), createLinesForItemSet(set)));
        writeWorkBookToFile("All_Collections.xlsx", workBook);
    }

    private void writeSouvenirCollections() {
        Workbook workBook = new XSSFWorkbook();
        List<ItemSet> collectionSets = itemSetService.search("Mirage", "Dust II", "Ancient", "Inferno",
            "Overpass", "Nuke", "Vertigo", "Cache", "Cobblestone", "Train", "Souvenir");
        collectionSets.forEach(set -> createSheet(workBook, set.getName(), createLinesForItemSet(set)));
        writeWorkBookToFile("Souvenir_Collections.xlsx", workBook);
    }

    private List<String[]> createLinesForItemSet(ItemSet set) {
        List<ItemName> allValidItemNames = itemService.getAllNamesForSet(set);

        List<String[]> lines = new ArrayList<>();
        List<String[]> finalLines = lines;
        AtomicInteger index = new AtomicInteger(1);
        String[] titleArray = new String[20];

        titleArray[0] = "Item Name";
        titleArray[1] = "Total Amount";

        List<Exterior> exteriors = itemSetService.getExteriorsForItemSet(set);
        if (exteriors.contains(Exterior.FACTORY_NEW) || exteriors.contains(Exterior.MINIMAL_WEAR) || exteriors.contains(Exterior.FIELD_TESTED) || exteriors.contains(Exterior.WELL_WORN) || exteriors.contains(Exterior.BATTLE_SCARRED)) {
            exteriors.addAll(Exterior.getBaseExteriors());
            exteriors = exteriors.stream().distinct().sorted(Comparator.comparingInt(Enum::ordinal)).collect(Collectors.toList());
        }

        boolean setHasStatTrak = itemSetService.hasStatTrakForItemSet(set);
        boolean setHasSouvenir = itemSetService.hasSouvenirForItemSet(set);

        if (setHasStatTrak && setHasSouvenir) {
            throw new IllegalStateException("Both StatTrak and Souvenir found for ItemSet " + set.getName());
        }

        if (setHasSouvenir || setHasStatTrak) {
            titleArray[2] = setHasSouvenir ? "Souvenir Amount" : "StatTrak Amount";
        }

        if (exteriors.size() > 0) {
            int exteriorIndex = 4;
            for (Exterior exterior : exteriors) {
                titleArray[exteriorIndex++] = "" + exterior.getName();
            }

            exteriorIndex++;

            if (setHasSouvenir || setHasStatTrak) {
                for (Exterior exterior : exteriors) {
                    titleArray[exteriorIndex++] = setHasSouvenir ? ("Souvenir " + exterior.getName()) : ("StatTrak " + exterior.getName());
                }
            }
        }

        int totalAmount = allValidItemNames.size();
        List<Exterior> finalExteriors = exteriors;
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently mapping item " + index.getAndIncrement() + " of " + totalAmount + ": " + itemName);
            List<Item> allItemsForName = itemService.getItemsForName(itemName);

            if (allItemsForName.size() == 0) {
                return;
            }

            String[] currentLine = formatLineForItems(itemName, allItemsForName, setHasStatTrak, setHasSouvenir, finalExteriors);

            synchronized (this) {
                finalLines.add(currentLine);
            }
        });

        lines = lines.stream().sorted(Comparator.comparingInt(v -> Integer.parseInt(((String[]) v)[1])).reversed()).collect(Collectors.toList());
        lines.add(0, titleArray);

        return lines;
    }

    @Transactional
    List<String[]> createLinesForItemSearch(String... filters) {
        List<ItemName> allValidItemNames = new ArrayList<>();

        for (String filter : filters) {
            allValidItemNames.addAll(itemNameService.getSearch(filter));
        }

        allValidItemNames = allValidItemNames.stream().distinct().collect(Collectors.toList());

        List<String[]> lines = new ArrayList<>();
        List<String[]> finalLines = lines;
        AtomicInteger index = new AtomicInteger(1);
        int totalAmount = allValidItemNames.size();
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently mapping item " + index.getAndIncrement() + " of " + totalAmount + ": " + itemName);
            List<Item> allItemsForName = itemService.getItemsForName(itemName);

            if (allItemsForName.size() == 0) {
                return;
            }

            String[] currentLine = formatLineForItems(itemName, allItemsForName, false, false, null);

            synchronized (this) {
                finalLines.add(currentLine);
            }
        });

        // sort by total amount
        lines = lines.stream().sorted(Comparator.comparingInt(v -> Integer.parseInt(((String[]) v)[1])).reversed()).collect(Collectors.toList());
        lines.add(0, new String[]{"Item name", "Total Amount"});

        return lines;
    }

    private void createSheet(Workbook workbook, String title, List<String[]> lines) {
        Sheet sheet = workbook.createSheet(title.replaceAll("[:/\\\\?*,.\\[\\]]", "")); // remove forbidden characters

        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Serif");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue(title);
        headerCell.setCellStyle(headerStyle);

        for (int i = 1; i < 20; i++) {
            Cell cell = header.createCell(i);
            cell.setCellStyle(headerStyle);
        }

        String[] firstRow = lines.get(0);
        if (firstRow != null) {
            Row columnNames = sheet.createRow(1);
            for (int i = 0; i < firstRow.length; i++) {
                var cell = columnNames.createCell(i);
                cell.setCellValue(firstRow[i]);
            }
        }

        // auto size all columns
        // 1.14388 is the max character width in serif
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            int maxChars = lines.stream().filter(line -> line != null && line.length > finalI).map(line -> line[finalI]).filter(Objects::nonNull).map(String::length).max(Comparator.comparingInt(v -> v)).orElse(-1);

            // if the title is longer than any value in the cells
            if (firstRow != null && firstRow.length > i && firstRow[i] != null) {
                if (firstRow[i].length() > maxChars) {
                    maxChars = firstRow[i].length();
                }
            }

            // in this case there is no data in this column, so we don't edit the column
            if (maxChars == -1) {
                sheet.setColumnWidth(i, 256 * 10);
                continue;
            }

            sheet.setColumnWidth(i, (int) (maxChars * 1.14388) * 256);
        }

        for (int i = 1; i < lines.size(); i++) {
            Row row = sheet.createRow(firstRow == null ? i + 1 : i + 2);
            String[] cells = lines.get(i);
            if (cells == null) {
                continue;
            }
            for (int j = 0; j < cells.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(cells[j]);
            }
        }

        LOGGER.info("Created sheet " + title + " in workbook.");
    }

    private void writeWorkBookToFile(String fileName, Workbook workbook) {
        LOGGER.info("Writing file " + fileName);

        String directoryName = "C:\\Users\\mitch\\Documents\\GitHub\\CSGODatabaseSpring\\output";

        new File(directoryName).mkdir();
        File file = new File(directoryName, fileName);

        try (OutputStream outputWriter = new FileOutputStream(file)) {
            workbook.write(outputWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] formatLineForItems(ItemName itemName, List<Item> items, boolean hasStatTrak, boolean hasSouvenir, List<Exterior> possibleExteriors) {
        String[] line = new String[20];
        // name
        line[0] = itemName.getName();
        // total count
        line[1] = "" + items.stream().map(Item::getAmount).mapToInt(v -> v).sum();
        // souvenir or stattrak count

        if (hasSouvenir) {
            int amount = items.stream().map(item -> {
                if (item.isSouvenir()) {
                    return item.getAmount();
                }
                return 0;
            }).mapToInt(v -> v).sum();
            if (amount > 0) {
                line[2] = "" + amount;
            }
        }

        if (hasStatTrak) {
            int amount = items.stream().map(item -> {
                if (item.isStatTrak()) {
                    return item.getAmount();
                }
                return 0;
            }).mapToInt(v -> v).sum();
            if (amount > 0) {
                line[2] = "" + amount;
            }
        }

        // some items inside the collection might not have an exterior
        if (possibleExteriors != null && items.get(0).getExterior() != null) {
            int index = 4;
            for (Exterior exterior : possibleExteriors) {
                line[index++] = "" + items.stream().filter(item -> item.getExterior() == exterior && !item.isSouvenir() && !item.isStatTrak()).map(Item::getAmount).mapToInt(v -> v).sum();
            }

            index++;

            if (hasSouvenir || hasStatTrak) {
                for (Exterior exterior : possibleExteriors) {
                    line[index++] = "" + items.stream().filter(item -> item.getExterior() == exterior && (item.isSouvenir() || item.isStatTrak())).map(Item::getAmount).mapToInt(v -> v).sum();
                }
            }
        }

        return line;
    }

    private String formatNumber(long number) {
        return JspHelper.formatNumber(number).replaceAll(",", ".");
    }

    private void addLine(List<String[]> list, String... text) {
        list.add(text);
    }

    private void emptyLine(List<String[]> list) {
        emptyLines(list, 1);
    }

    private void emptyLines(List<String[]> list, int l) {
        for (int i = 0; i < l; i++) {
            addLine(list, "");
        }
    }
}