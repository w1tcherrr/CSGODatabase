package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
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
    private final ItemNameService itemNameService;

    public DataWriter(CSGOAccountService CSGOAccountService,
                      ItemService itemService,
                      ItemNameService itemNameService) {
        this.CSGOAccountService = CSGOAccountService;
        this.itemService = itemService;
        this.itemNameService = itemNameService;
    }

    public void write() {
        LOGGER.info("Writing Data now");

        //writeAll();
        writeMajors();
    }

    private void writeAll() {
        LOGGER.info("Writing All Data");
        Workbook workBook = new XSSFWorkbook();
        createSheet(workBook, "Combined Data", createLinesForSearch(""));
        writeWorkBookToFile("Combined_Data.xlsx", workBook);
    }

    private void writeMajors() {
        LOGGER.info("Writing Major Data");
        Workbook workBook = new XSSFWorkbook();
        createSheet(workBook, "Antwerp 2022", createLinesForSearch("Antwerp 2022"));
        createSheet(workBook, "Stockholm 2021", createLinesForSearch("Stockholm 2021"));
        createSheet(workBook, "Berlin 2019", createLinesForSearch("Berlin 2019"));
        createSheet(workBook, "Katowice 2019", createLinesForSearch("Katowice 2019"));
        createSheet(workBook, "London 2018", createLinesForSearch("London 2018"));
        createSheet(workBook, "Boston 2018", createLinesForSearch("Boston 2018"));
        createSheet(workBook, "Krakow 2017", createLinesForSearch("Krakow 2017"));
        createSheet(workBook, "Atlanta 2017", createLinesForSearch("Atlanta 2017"));
        createSheet(workBook, "Cologne 2016", createLinesForSearch("Cologne 2016"));
        createSheet(workBook, "Columbus 2016", createLinesForSearch("Columbus 2016"));
        createSheet(workBook, "Cluj-Napoca 2015", createLinesForSearch("Cluj-Napoca 2015"));
        createSheet(workBook, "Cologne 2015", createLinesForSearch("Cologne 2015"));
        createSheet(workBook, "Katowice 2015", createLinesForSearch("Katowice 2015"));
        createSheet(workBook, "DreamHack Winter 2014", createLinesForSearch("DreamHack Winter 2014", "DreamHack 2014"));
        createSheet(workBook, "Cologne 2014", createLinesForSearch("Cologne 2014"));
        createSheet(workBook, "Katowice 2014", createLinesForSearch("Katowice 2014"));
        createSheet(workBook, "DreamHack Winter 2013", createLinesForSearch("DreamHack 2013", "DreamHack Winter 2013"));
        writeWorkBookToFile("All_Major_Items.xlsx", workBook);
    }

    @Transactional
    List<String[]> createLinesForSearch(String... filters) {
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

            String[] currentLine = new String[10];
            List<Item> allItemsForName = itemService.getItemsForName(itemName);

            currentLine[0] = itemName.getName();
            currentLine[1] = "" + allItemsForName.stream().map(Item::getAmount).mapToInt(v -> v).sum();

            synchronized (this) {
                finalLines.add(currentLine);
            }
        });

        // sort by total amount
        lines = lines.stream().sorted(Comparator.comparingInt(v -> Integer.parseInt(((String[]) v)[1])).reversed()).collect(Collectors.toList());

        return lines;
    }

    private void createSheet(Workbook workbook, String title, List<String[]> lines) {
        Sheet sheet = workbook.createSheet(title);

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

        // auto size all columns
        // 1.14388 is the max character width in serif
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            int maxChars = lines.stream().filter(line -> line.length > finalI).map(line -> line[finalI]).filter(Objects::nonNull).map(String::length).max(Comparator.comparingInt(v -> v)).orElse(-1);
            if (maxChars == -1) {
                continue;
            }
            sheet.setColumnWidth(i, (int) (maxChars * 1.14388) * 256);
        }

        for (int i = 1; i <= lines.size(); i++) {
            Row row = sheet.createRow(i);
            String[] cells = lines.get(i - 1);
            for (int j = 0; j < cells.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(cells[j]);
            }
        }

        LOGGER.info("Created sheet " + title + " in workbook.");
    }

    private void writeWorkBookToFile(String fileName, Workbook workbook) {
        String directoryName = "C:\\Users\\mitch\\Documents\\GitHub\\CSGODatabaseSpring\\output";

        new File(directoryName).mkdir();
        File file = new File(directoryName, fileName);

        try (OutputStream outputWriter = new FileOutputStream(file)) {
            workbook.write(outputWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}