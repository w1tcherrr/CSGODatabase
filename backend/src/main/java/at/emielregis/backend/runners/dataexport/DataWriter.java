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
import java.util.Comparator;
import java.util.List;
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

        // write data for all normal item collections
        runThread(this::writeCollections);

        // write cases
        runThread(this::writeCases);

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
        createLinesForItemSearch("").forEach(builder::addRow);
        builder.setTitleRow("Combined Data");
        builder.setDescriptionRow("Item Name", "Total Count");
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
            new String[]{"Berlin 2019", "Berlin 2019"},
            new String[]{"Katowice 2019", "Katowice 2019"},
            new String[]{"London 2018", "London 2018"},
            new String[]{"Boston 2018", "Boston 2018"},
            new String[]{"Krakow 2017", "Krakow 2018"},
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
            createLinesForItemSearch(searches).forEach(builder::addRow);
        }

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

        for (ItemSet set : collectionSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("All_Collections.xlsx", workBook);
    }

    private void writeSouvenirCollections() {
        Workbook workBook = new XSSFWorkbook();

        List<ItemSet> collectionSets = itemSetService.searchBySubstring("Mirage", "Dust II", "Ancient", "Inferno",
            "Overpass", "Nuke", "Vertigo", "Cache", "Cobblestone", "Train", "Souvenir");

        for (ItemSet set : collectionSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("Souvenir_Collections.xlsx", workBook);
    }

    private void writeCases() {
        Workbook workBook = new XSSFWorkbook();

        // all case sets
        List<ItemSet> caseSets = itemSetService.searchByEquality(
            "The Recoil Collection",
            "The Dreams & Nightmares Collection",
            "The Operation Riptide Collection",
            "The Snakebite Collection",
            "The Operation Broken Fang Collection",
            "The Fracture Collection",
            "The Prisma 2 Collection",
            "The Prisma Collection",
            "The CS20 Collection",
            "The Shattered Web Collection",
            "The Danger Zone Collection",
            "The Horizon Collection",
            "The Clutch Collection",
            "The Spectrum 2 Collection",
            "The Spectrum Collection",
            "The Operation Hydra Collection",
            "The Glove Collection",
            "The Gamma 2 Collection",
            "The Gamma Collection",
            "The Chroma Collection",
            "The Chroma 2 Collection",
            "The Chroma 3 Collection",
            "The Wildfire Collection",
            "The Revolver Case Collection",
            "The Shadow Collection",
            "The Falchion Collection",
            "The Vanguard Collection",
            "The eSports 2014 Summer Collection",
            "The Breakout Collection",
            "The Huntsman Collection",
            "The Phoenix Collection",
            "The Arms Deal Collection",
            "The Arms Deal 2 Collection",
            "The Arms Deal 3 Collection",
            "The Winter Offensive Collection",
            "The eSports 2013 Winter Collection",
            "The eSports 2013 Collection",
            "The Bravo Collection"
        );

        for (ItemSet set : caseSets) {
            SheetBuilder builder = SheetBuilder.create(workBook, set.getName());
            List<String[]> lines = createLinesForItemSet(set);
            builder.setTitleRow(set.getName());
            builder.setDescriptionRow(lines.get(0));
            lines.subList(1, lines.size()).forEach(builder::addRow);
        }

        writeWorkBookToFile("Cases.xlsx", workBook);
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

        return lines;
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
}