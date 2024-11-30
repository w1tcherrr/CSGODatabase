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

/**
 * Abstract base class for exporting data to Excel workbooks.
 * Provides shared methods and utilities for generating and writing data to Excel files.
 */
public abstract class AbstractDataWriter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DIRECTORY_PATH = "C:\\Users\\misch\\IdeaProjects\\CSGODatabase\\output";

    // Services required for data processing and writing
    protected final ItemService itemService;
    protected final SteamAccountService steamAccountService;
    protected final CSGOAccountService csgoAccountService;
    protected final StickerService stickerService;
    protected final CharmService charmService;
    protected final ItemSetService itemSetService;
    protected final ItemNameService itemNameService;
    protected final ItemCategoryService itemCategoryService;
    protected final ItemTypeService itemTypeService;

    /**
     * Constructor for initializing the data writer with required services.
     */
    public AbstractDataWriter(ItemService itemService, SteamAccountService steamAccountService, CSGOAccountService csgoAccountService, CharmService charmService,
                              StickerService stickerService, ItemSetService itemSetService, ItemNameService itemNameService, ItemCategoryService itemCategoryService, ItemTypeService itemTypeService) {
        this.itemService = itemService;
        this.steamAccountService = steamAccountService;
        this.csgoAccountService = csgoAccountService;
        this.stickerService = stickerService;
        this.charmService = charmService;
        this.itemSetService = itemSetService;
        this.itemNameService = itemNameService;
        this.itemCategoryService = itemCategoryService;
        this.itemTypeService = itemTypeService;
    }

    /**
     * Writes data to an Excel workbook and saves it to a file.
     *
     * @param fileName The name of the output file.
     */
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

    /**
     * Abstract method to be implemented by subclasses for writing specific data to the workbook.
     *
     * @param workbook The workbook to write data to.
     */
    protected abstract void writeWorkbook(Workbook workbook);

    /**
     * Checks if a file with the given name has already been exported.
     *
     * @param fileName The name of the file to check.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    private static boolean isAlreadyExported(String fileName) {
        File file = new File(DIRECTORY_PATH, fileName);
        return file.exists();
    }

    /**
     * Writes the workbook to a file in the specified directory.
     *
     * @param fileName The name of the output file.
     * @param workbook The workbook to write.
     */
    private static synchronized void writeWorkBookToFile(String fileName, Workbook workbook) {
        LOGGER.info("Writing file " + fileName);

        SheetBuilder.getBuildersForWorkbook(workbook).forEach(SheetBuilder::build);

        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists() && !directory.mkdir()) {
            throw new RuntimeException("Output directory could not be created! Please check permissions and the path.");
        }

        File file = new File(DIRECTORY_PATH, fileName);

        try (OutputStream outputWriter = new FileOutputStream(file)) {
            workbook.write(outputWriter);
        } catch (IOException e) {
            LOGGER.error("Error writing workbook to file", e);
        }
    }

    /**
     * Creates lines of data for items based on search filters.
     *
     * @param filters Search filters for item names.
     * @return A list of formatted data lines.
     */
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

        sortByNumericalColumn(lines, 1);
        return lines;
    }

    /**
     * Sorts lines of data by a numerical column in descending order.
     *
     * @param lines        The data lines to sort.
     * @param columnNumber The index of the column to sort by.
     */
    protected static void sortByNumericalColumn(List<String[]> lines, int columnNumber) {
        LOGGER.info("AbstractDataWriter#sortByColumn()");
        lines.sort(Comparator.comparingDouble(a -> getNumber(a[columnNumber])));
        Collections.reverse(lines);
    }

    /**
     * Parses a string to a numeric value for sorting.
     *
     * @param number The string to parse.
     * @return The numeric value, or 0 if parsing fails.
     */
    private static double getNumber(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Creates lines of data for a specific item set.
     *
     * @param set The item set to process.
     * @return A list of formatted data lines for the item set.
     */
    protected List<String[]> createLinesForItemSet(ItemSet set) {
        LOGGER.info("AbstractDataWriter#createLinesForItemSet(" + set + ")");
        List<ItemName> allValidItemNames = itemTypeService.getAllNamesForSet(set);
        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);

        List<Exterior> exteriors = Exterior.extendBaseExteriors(itemSetService.getExteriorsForItemSet(set));
        boolean setHasStatTrak = itemSetService.hasStatTrakForItemSet(set);
        boolean setHasSouvenir = itemSetService.hasSouvenirForItemSet(set);

        int totalAmount = allValidItemNames.size();
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Analysing item " + index.getAndIncrement() + "/" + totalAmount + ": " + itemName);
            lines.add(formatLineForItemName(itemName, setHasStatTrak, setHasSouvenir, exteriors));
        });

        sortByNumericalColumn(lines, 1);
        return lines;
    }

    /**
     * Formats a line of data for an item name.
     *
     * @param itemName         The item name to process.
     * @param hasStatTrak      Whether the item set includes StatTrak items.
     * @param hasSouvenir      Whether the item set includes Souvenir items.
     * @param possibleExteriors The list of possible exteriors for the item.
     * @return A formatted data line.
     */
    protected String[] formatLineForItemName(ItemName itemName, boolean hasStatTrak, boolean hasSouvenir, List<Exterior> possibleExteriors) {
        String[] line = new String[20];
        line[0] = itemName.getName();
        line[1] = String.valueOf(itemService.getTotalAmountForName(itemName));

        if (hasSouvenir || hasStatTrak) {
            line[2] = String.valueOf(hasSouvenir ? itemService.getSouvenirAmountForName(itemName) : itemService.getStatTrakAmountForName(itemName));
        }

        if (possibleExteriors != null && itemNameService.itemNameHasExteriors(itemName)) {
            int index = 4;
            for (Exterior exterior : possibleExteriors) {
                line[index++] = String.valueOf(itemService.countForExteriorAndType(itemName, exterior, false, false));
            }
        }

        return line;
    }

    /**
     * Creates a styled cell for item names.
     *
     * @param workbook   The workbook containing the style.
     * @param itemNameName The name of the item.
     * @return A styled {@link CellStyle}.
     */
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
            cellStyle.setFillForegroundColor(itemNameService.getRarityForItemNameName(itemNameName).getColor().getIndex());
        }
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }
}
