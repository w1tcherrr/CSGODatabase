package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.data.entities.ItemName;
import at.emielregis.backend.data.entities.ItemSet;
import at.emielregis.backend.data.enums.Exterior;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemCategoryService;
import at.emielregis.backend.service.ItemNameService;
import at.emielregis.backend.service.ItemService;
import at.emielregis.backend.service.ItemSetService;
import at.emielregis.backend.service.SteamAccountService;
import at.emielregis.backend.service.StickerService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static at.emielregis.backend.runners.dataexport.DataWriterUtils.writeWorkBookToFile;

@Component
@Transactional
public class DataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ItemService itemService;
    private final ItemSetService itemSetService;
    private final ItemNameService itemNameService;
    private final CSGOAccountService csgoAccountService;
    private final SteamAccountService steamAccountService;
    private final StickerService stickerService;
    private final ItemCategoryService itemCategoryService;
    private final List<Thread> threads = new ArrayList<>();

    public DataWriter(ItemService itemService,
                      ItemSetService itemSetService,
                      ItemNameService itemNameService,
                      CSGOAccountService csgoAccountService,
                      SteamAccountService steamAccountService,
                      StickerService stickerService,
                      ItemCategoryService itemCategoryService) {
        this.itemService = itemService;
        this.itemSetService = itemSetService;
        this.itemNameService = itemNameService;
        this.csgoAccountService = csgoAccountService;
        this.steamAccountService = steamAccountService;
        this.stickerService = stickerService;
        this.itemCategoryService = itemCategoryService;
    }

    public void write() throws InterruptedException {
        LOGGER.info("Writing Data now");

        // write all raw data into a single file
        runThread(this::writeAll);

        // write some miscellaneous data
        runThread(this::writeMiscellaneous);

        // write data for all majors
        runThread(this::writeMajors);

        // write data for all collections
        runThread(this::writeSouvenirCollections);
        runThread(this::writeStickerCollections);
        runThread(this::writePatches);

        // write cases
        runThread(this::writeCases);

        // write storage units
        runThread(this::writeStorageUnits);

        awaitAll();
    }

    private void runThread(Runnable r) {
        Thread t = new Thread(r);
        threads.add(t);
        t.start();
    }

    private void awaitAll() throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }

    private void writeAll() {
        LOGGER.info("Writing All Data");
        Workbook workBook = new XSSFWorkbook();
        SheetBuilder builder = SheetBuilder.create(workBook, "Combined Data");
        builder.setTitleRow("Combined Data");
        builder.setDescriptionRow("Item Name", "Total Count");
        createLinesForItemSearch("").forEach(builder::addRow);
        writeWorkBookToFile("Combined_Data.xlsx", workBook);
    }

    private void writeMiscellaneous() {
        LOGGER.info("Writing miscellaneous Data");

        Workbook workBook = new XSSFWorkbook();
        SheetBuilder builder = SheetBuilder.create(workBook, "Miscellaneous");
        builder.setTitleRow("Miscellaneous");

        // total amounts
        long noStorageUnitCount = itemService.itemCountNoStorageUnits();
        long onlyStorageUnitCount = itemService.itemCountOnlyStorageUnits();
        long totalCount = noStorageUnitCount + onlyStorageUnitCount;

        builder.emptyLines(1);

        builder.addRow("Total Steam-Accounts queried:", "" + (steamAccountService.count()));
        builder.addRow("Total CSGO-Accounts queried:", "" + (csgoAccountService.count()));
        builder.addRow("Total CSGO-Accounts with inventories queried:", "" + (csgoAccountService.countWithInventory()));
        builder.emptyLines(1);

        builder.addRow("Total Items (no Storage Units):", "" + (noStorageUnitCount));
        builder.addRow("Total Items in Storage Units:", "" + (onlyStorageUnitCount));
        builder.addRow("Total Items:", "" + (totalCount));
        builder.emptyLines(1);

        builder.addRow("Most Storage Units in one inventory:", "" + (itemService.getHighestStorageUnitCount()));
        builder.addRow("Most full Storage Units in one inventory:", "" + (itemService.getHighestFullStorageUnitCount()));
        builder.addRow("Most items in one inventory:", "" + (itemService.getHighestSingleInventoryCount()));
        builder.addRow("Least items in one inventory:", "" + (itemService.getLowestSingleInventoryCount()));
        builder.addRow("Average items per inventory:", "" + (totalCount / csgoAccountService.countWithInventory()));
        builder.emptyLines(1);

        builder.addRow("Total amount of item sets:", "" + (itemSetService.count()));
        builder.addRow("Total amount of item categories:", "" + (itemCategoryService.count()));
        builder.addRow("Total amount of different items:", "" + (itemNameService.count()));
        builder.addRow("Total amount of different non-applied stickers:", "" + (stickerService.countNonApplied()));
        builder.addRow("Total amount of different applied stickers:", "" + (stickerService.count()), "<- Note: This number is higher due to old unobtainable Souvenir stickers");
        builder.emptyLines(1);

        builder.addRow("Total applied stickers:", "" + (stickerService.appliedStickerCount()));

        writeWorkBookToFile("Miscellaneous_Data.xlsx", workBook);
    }

    private void writeMajors() {
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

        Workbook workBook = new XSSFWorkbook();

        for (String[] strings : majorList) {
            SheetBuilder builder = SheetBuilder.create(workBook, strings[0]);
            String[] searches = Arrays.copyOfRange(strings, 1, strings.length);
            builder.setTitleRow(strings[0]);
            builder.setDescriptionRow("Item Name", "Total Amount");
            createLinesForItemSearch(searches).forEach(builder::addRow);
        }

        writeWorkBookToFile("Major_Collections.xlsx", workBook);
    }

    private void writeSouvenirCollections() {
        Workbook workBook = new XSSFWorkbook();

        List<ItemSet> collectionSets = itemSetService.getAllSouvenirCollections();

        for (ItemSet set : collectionSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("Souvenir_Collections.xlsx", workBook);
    }

    private void writeStickerCollections() {
        LOGGER.info("Writing Sticker Collections");
        Workbook workBook = new XSSFWorkbook();

        List<ItemSet> stickerSets = itemSetService.getAllStickerCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Collection", "Total Amount (Non applied)", "Total Amount (Applied)");

        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (ItemSet set : stickerSets) {
            LOGGER.info("Currently mapping total amounts of set " + current.incrementAndGet() + "/" + stickerSets.size() + ": " + set.getName());
            rows.add(new String[]{set.getName(), "" + itemService.getTotalAmountForSet(set), "" + stickerService.getTotalAppliedForSet(set)});
        }

        sortByColumn(rows, 1);
        rows.forEach(overviewBuilder::addRow);
        overviewBuilder.emptyLines(1);
        overviewBuilder.addRow("Note: A few old capsules are not identified by the API - their amount are not listed here.");

        SheetBuilder unclassifiedBuilder = SheetBuilder.create(workBook, "Unclassified");
        unclassifiedBuilder.setTitleRow("Unclassified Stickers");
        unclassifiedBuilder.setDescriptionRow("Item Name", "Total Amount");

        List<ItemName> unclassifiedStickerNames = itemNameService.getUnclassifiedStickerNames();

        rows = new ArrayList<>();
        List<String[]> finalRows = rows;
        current = new AtomicInteger();
        AtomicInteger finalCurrent = current;
        unclassifiedStickerNames.forEach(name -> {
            LOGGER.info("Currently mapping item " + finalCurrent.incrementAndGet() + "/" + unclassifiedStickerNames.size() + ": " + name.getName());
            finalRows.add(formatLineForItemName(name, false, false, List.of()));
        });

        sortByColumn(rows, 1);
        rows.forEach(unclassifiedBuilder::addRow);

        for (ItemSet set : stickerSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines = lines.subList(1, lines.size());
            lines.forEach(line -> line[2] = "" + stickerService.getTotalAppliedForItemName(line[0]));
            lines.forEach(builder::addRow);
        }

        writeWorkBookToFile("Sticker_Collections.xlsx", workBook);
    }

    private void writePatches() {
        LOGGER.info("Writing Patch Collections");
        Workbook workBook = new XSSFWorkbook();

        List<ItemSet> patchSets = itemSetService.getAllPatchCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Collection", "Total Amount");

        List<String[]> rows = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (ItemSet set : patchSets) {
            LOGGER.info("Currently mapping total amount of set " + current.incrementAndGet() + "/" + patchSets.size() + ": " + set.getName());
            rows.add(new String[]{set.getName(), "" + itemService.getTotalAmountForSet(set)});
        }

        sortByColumn(rows, 1);
        rows.forEach(overviewBuilder::addRow);

        for (ItemSet set : patchSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("Patch_Collections.xlsx", workBook);
    }

    private void writeCases() {
        Workbook workBook = new XSSFWorkbook();

        List<ItemSet> caseSets = itemSetService.getAllCaseCollections();

        SheetBuilder overviewBuilder = SheetBuilder.create(workBook, "Overview");
        overviewBuilder.setTitleRow("Overview");
        overviewBuilder.setDescriptionRow("Item Name", "Amount of Cases", "Total amount of items from Collection");

        List<String[]> overviewLines = new ArrayList<>();
        for (ItemSet set : caseSets) {
            List<ItemName> allValidItemNames = itemService.getAllNamesForSet(set);
            for (ItemName name : allValidItemNames) {
                if (name.getName().matches(".* Case ?[23]?")) {
                    overviewLines.add(new String[]{name.getName(), "" + itemService.getTotalAmountForName(name), "" + itemService.getTotalAmountForSet(set)});
                }
            }
        }

        sortByColumn(overviewLines, 1);
        overviewLines.forEach(overviewBuilder::addRow);

        for (ItemSet set : caseSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("Case_Collections.xlsx", workBook);
    }

    @Transactional
    protected void writeStorageUnits() {
        Workbook workBook = new XSSFWorkbook();

        SheetBuilder storageUnitSheetBuilder = SheetBuilder.create(workBook, "Overview");
        storageUnitSheetBuilder.setTitleRow("Overview");
        storageUnitSheetBuilder.setDescriptionRow("Storage Unit Name", "Amount of Units with name", "Total amount of items");

        storageUnitSheetBuilder.addRow("Empty units", "" + itemService.getTotalAmountOfEmptyStorageUnits());
        storageUnitSheetBuilder.emptyLines(1);

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());

        List<String> nameTags = itemService.getAllStorageUnitNameTags().stream().sorted(Comparator.comparing(str -> str)).toList();
        AtomicInteger index = new AtomicInteger(-1);
        int size = nameTags.size();
        nameTags.parallelStream().forEach(tag -> {
                if (index.incrementAndGet() % 100 == 0) {
                    LOGGER.info("Mapped " + index.get() + " names of " + size);
                }
                String[] line = new String[3];
                line[0] = tag;

                // ironically fetching the entities and counting via java instead of in the database is more efficient here
                // due to the big size of the table. The count query takes up twice as much time.
                List<Item> storageUnits = itemService.getStorageUnitsForNameTag(tag);
                line[1] = "" + storageUnits.stream().mapToInt(Item::getAmount).sum();
                line[2] = "" + storageUnits.stream().mapToInt(Item::getStorageUnitAmount).filter(Objects::nonNull).sum();

                // we don't care about empty units
                if (Integer.parseInt(line[2]) == 0) {
                    return;
                }

                lines.add(line);
            }
        );

        sortByColumn(lines, 2);
        lines.forEach(storageUnitSheetBuilder::addRow);

        writeWorkBookToFile("Storage_Units.xlsx", workBook);
    }

    private void sortByColumn(List<String[]> lines, int columnNumber) {
        lines.sort(Comparator.comparingInt(a -> Integer.parseInt(a[columnNumber])));
        Collections.reverse(lines);
    }

    private List<String[]> createLinesForItemSet(ItemSet set) {
        List<ItemName> allValidItemNames = itemService.getAllNamesForSet(set);

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);
        String[] titleArray = new String[20];

        titleArray[0] = "Item Name";
        titleArray[1] = "Total Amount";

        List<Exterior> exteriors = itemSetService.getExteriorsForItemSet(set);

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
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently analysing item " + index.getAndIncrement() + " of " + totalAmount + ": " + itemName);

            String[] currentLine = formatLineForItemName(itemName, setHasStatTrak, setHasSouvenir, exteriors);

            lines.add(currentLine);
        });

        sortByColumn(lines, 1);
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

        List<String[]> lines = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger index = new AtomicInteger(1);
        int totalAmount = allValidItemNames.size();
        allValidItemNames.parallelStream().forEach(itemName -> {
            LOGGER.info("Currently analysing item " + index.getAndIncrement() + " of " + totalAmount + ": " + itemName);

            String[] currentLine = formatLineForItemName(itemName, false, false, null);

            lines.add(currentLine);
        });

        // sort by total amount
        sortByColumn(lines, 1);

        return lines;
    }

    private String[] formatLineForItemName(ItemName itemName, boolean hasStatTrak, boolean hasSouvenir, List<Exterior> possibleExteriors) {
        String[] line = new String[20];
        // name
        line[0] = itemName.getName();
        // total count
        line[1] = "" + itemService.getTotalAmountForName(itemName);
        // souvenir or stattrak count

        if (hasSouvenir || hasStatTrak) {
            long amount = itemService.getSouvenirOrStatTrakAmountForName(itemName);
            if (amount > 0) {
                line[2] = "" + amount;
            }
        }

        // some items inside the collection might not have an exterior
        if (possibleExteriors != null && itemService.itemNameHasExteriors(itemName)) {
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
}