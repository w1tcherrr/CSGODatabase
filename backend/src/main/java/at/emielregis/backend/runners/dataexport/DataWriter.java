package at.emielregis.backend.runners.dataexport;

import at.emielregis.backend.data.entities.Item;
import at.emielregis.backend.service.CSGOAccountService;
import at.emielregis.backend.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Transactional
public class DataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CSGOAccountService CSGOAccountService;
    private final ItemService itemService;
    private Map<Item, List<Integer>> combinedItemMap = null;

    public DataWriter(CSGOAccountService CSGOAccountService,
                      ItemService itemService) {
        this.CSGOAccountService = CSGOAccountService;
        this.itemService = itemService;
    }

    public void write() {
        LOGGER.info("Writing Data now");

        writeAll();
        writeMajors();

        combinedItemMap = null;
    }

    private void writeAll() {
        LOGGER.info("Writing All Data");
        writeToFile("all.txt", getStringsForItemName("All", ""));
    }

    private void writeMajors() {
        LOGGER.info("Writing Major Data");
        writeToFile("antwerp2022.txt", getStringsForItemName("Antwerp 2022", "Antwerp 2022"));
        writeToFile("stockholm2021.txt", getStringsForItemName("Stockholm 2021", "Stockholm 2021"));
        writeToFile("berlin2019.txt", getStringsForItemName("Berlin 2019", "Berlin 2019"));
        writeToFile("katowice2019.txt", getStringsForItemName("Katowice 2019", "Katowice 2019"));
        writeToFile("london2018.txt", getStringsForItemName("London 2018", "London 2018"));
        writeToFile("boston2018.txt", getStringsForItemName("Boston 2018", "Boston 2018"));
        writeToFile("krakow2017.txt", getStringsForItemName("Krakow 2017", "Krakow 2017"));
        writeToFile("atlanta2017.txt", getStringsForItemName("Atlanta 2017", "Atlanta 2017"));
        writeToFile("cologne2016.txt", getStringsForItemName("Cologne 2016", "Cologne 2016"));
        writeToFile("columbus2016.txt", getStringsForItemName("Columbus 2016", "Columbus 2016"));
        writeToFile("cluj_napoca2015.txt", getStringsForItemName("Cluj-Napoca 2015", "Cluj-Napoca 2015"));
        writeToFile("cologne2015.txt", getStringsForItemName("Cologne 2015", "Cologne 2015"));
        writeToFile("katowice2015.txt", getStringsForItemName("Katowice 2015", "Katowice 2015"));
        writeToFile("dreamhack_winter_2014.txt", getStringsForItemName("DreamHack Winter 2014", "DreamHack Winter 2014", "DreamHack 2014"));
        writeToFile("cologne2014.txt", getStringsForItemName("Cologne 2014", "Cologne 2014"));
        writeToFile("katowice2014.txt", getStringsForItemName("Katowice 2014", "Katowice 2014"));
        writeToFile("dream_winter_2013.txt", getStringsForItemName("DreamHack Winter 2013", "DreamHack 2013", "DreamHack Winter 2013"));
    }

    private List<String> getStringsForItemName(String s, String... filters) {
        return new ArrayList<>();
    }

    private void writeToFile(String fileName, List<String> lines) {
        String directoryName = "C:\\Users\\mitch\\Documents\\GitHub\\CSGODatabaseSpring\\output";

        new File(directoryName).mkdir();
        File file = new File(directoryName, fileName);

        try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                outputWriter.write(line);
                outputWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}