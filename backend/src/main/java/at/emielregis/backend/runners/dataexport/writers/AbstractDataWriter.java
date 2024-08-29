package at.emielregis.backend.runners.dataexport.writers;

import at.emielregis.backend.data.entities.items.ItemName;
import at.emielregis.backend.data.entities.items.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.runners.dataexport.SheetBuilder;
import at.emielregis.backend.service.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractDataWriter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DIRECTORY_PATH = "C:\\Users\\misch\\IdeaProjects\\CSGODatabase\\output";

    protected final ItemService itemService;
    protected final SteamAccountService steamAccountService;
    protected final CSGOAccountService csgoAccountService;
    protected final StickerService stickerService;
    protected final ItemSetService itemSetService;
    protected final ItemNameService itemNameService;
    protected final ItemCategoryService itemCategoryService;
    protected final ItemTypeService itemTypeService;

    public AbstractDataWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService,
                              StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ItemTypeService itemTypeService) {
        this.itemService = itemService;
        this.steamAccountService = steamAccountService;
        this.csgoAccountService = csgoAccountService;
        this.stickerService = stickerService;
        this.itemSetService = itemSetService;
        this.itemNameService = itemNameService;
        this.itemCategoryService = itemCategoryService;
        this.itemTypeService = itemTypeService;
    }

    public void writeWorkbook(String fileName) {
        if (isAlreadyExported(fileName)) {
            LOGGER.info("Data for " + fileName + " already extracted");
            return;
        }

        LOGGER.info("Exporting Data for " + fileName);

        Workbook workBook = new XSSFWorkbook();
        writeWorkbook(workBook);
        writeWorkBookToFile(fileName, workBook);
    }

    protected abstract void writeWorkbook(Workbook workbook);

    private static boolean isAlreadyExported(String fileName) {
        File file = new File(DIRECTORY_PATH, fileName);
        return file.exists();
    }

    private static synchronized void writeWorkBookToFile(String fileName, Workbook workbook) {
        LOGGER.info("Writing file " + fileName);

        SheetBuilder.getBuildersForWorkbook(workbook).forEach(SheetBuilder::build);

        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            if (!new File(DIRECTORY_PATH).mkdir()) {
                throw new RuntimeException("Output directory could not be created! Please check whether the process has permission and the path is correct.");
            }
        }

        File file = new File(DIRECTORY_PATH, fileName);

        try (OutputStream outputWriter = new FileOutputStream(file)) {
            workbook.write(outputWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<String[]> createLinesForItemSearch(String... filters) {
        LOGGER.info("AbstractDataWriter#createLinesForItemSearch(" + Arrays.toString(filters) + ")");
        List<ItemName> allValidItemNames = itemNameService.getSearch(filters);

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);
        int totalAmount = allValidItemNames.size();
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Analysing item " + index.getAndIncrement() + "/" + totalAmount + ": " + itemName);

            String[] currentLine = formatLineForItemName(itemName, false, false, null);

            lines.add(currentLine);
        });

        // sort by total amount
        sortByNumericalColumn(lines, 1);

        return lines;
    }

    protected static void sortByNumericalColumn(List<String[]> lines, int columnNumber) {
        LOGGER.info("AbstractDataWriter#sortByColumn()");
        lines.sort(Comparator.comparingDouble(a -> getNumber(a[columnNumber])));
        Collections.reverse(lines);
    }

    private static double getNumber(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return Integer.parseInt(number);
        }
    }

    protected List<String[]> createLinesForItemSet(ItemSet set) {
        LOGGER.info("AbstractDataWriter#createLinesForItemSet(" + set.toString() + ")");

        List<ItemName> allValidItemNames = itemTypeService.getAllNamesForSet(set);

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);
        String[] titleArray = new String[20];

        titleArray[0] = "Item Name";
        titleArray[1] = "Total Amount";

        List<Exterior> exteriors = itemSetService.getExteriorsForItemSet(set);

        exteriors = Exterior.extendBaseExteriors(exteriors);

        boolean setHasStatTrak = itemSetService.hasStatTrakForItemSet(set);
        boolean setHasSouvenir = itemSetService.hasSouvenirForItemSet(set);

        if (setHasStatTrak && setHasSouvenir) {
            throw new IllegalStateException("Both StatTrak and Souvenir found for ItemSet " + set.getName());
        }

        if (setHasSouvenir || setHasStatTrak) {
            titleArray[2] = setHasSouvenir ? "Souvenir Amount" : "StatTrak Amount";
        }

        if (!exteriors.isEmpty()) {
            int exteriorIndex = 4;
            for (Exterior exterior : exteriors) {
                titleArray[exteriorIndex++] = exterior.getName();
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
            LOGGER.info("Analysing item " + index.getAndIncrement() + "/" + totalAmount + ": " + itemName);

            String[] currentLine = formatLineForItemName(itemName, setHasStatTrak, setHasSouvenir, finalExteriors);

            lines.add(currentLine);
        });

        sortByNumericalColumn(lines, 1);
        lines.add(0, titleArray);

        return lines;
    }

    protected String[] formatLineForItemName(ItemName itemName, boolean hasStatTrak, boolean hasSouvenir, List<Exterior> possibleExteriors) {
        String[] line = new String[20];
        // name
        line[0] = itemName.getName();
        // total count
        line[1] = "" + itemService.getTotalAmountForName(itemName);
        // souvenir or stattrak count

        if (hasSouvenir || hasStatTrak) {
            long amount;
            if (hasSouvenir) {
                amount = itemService.getSouvenirAmountForName(itemName);
            } else {
                amount = itemService.getStatTrakAmountForName(itemName);
            }
            if (amount > 0) {
                line[2] = "" + amount;
            }
        }

        // some items inside the collection might not have an exterior
        if (possibleExteriors != null && itemNameService.itemNameHasExteriors(itemName)) {
            int index = 4;

            for (Exterior exterior : possibleExteriors) {
                long amount = itemService.countForExteriorAndType(itemName, exterior, false, false);
                if (amount > 0) {
                    line[index++] = "" + amount;
                } else {
                    index++;
                }
            }

            index++;

            if (hasSouvenir || hasStatTrak) {
                for (Exterior exterior : possibleExteriors) {
                    long amount = itemService.countForExteriorAndType(itemName, exterior, hasStatTrak, hasSouvenir);
                    if (amount > 0) {
                        line[index++] = "" + amount;
                    } else {
                        index++;
                    }
                }
            }
        }

        return line;
    }

    protected CellStyle getStyleForName(Workbook workbook, String itemNameName) {
        LOGGER.info("AbstractDataWriter#getStyleForName(" + itemNameName + ")");
        CellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Serif");
        font.setFontHeightInPoints((short) 11);
        cellStyle.setFont(font);
        if (itemNameName.contains("★")) {
            cellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        } else {
            IndexedColors color = itemNameService.getRarityForItemNameName(itemNameName).getColor();
            cellStyle.setFillForegroundColor(color.getIndex());
        }
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }
}
