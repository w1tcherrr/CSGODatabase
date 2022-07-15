package at.emielregis.backend.runners.dataexport;

import org.apache.catalina.manager.JspHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

public class DataWriterUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DIRECTORY_PATH = "C:\\Users\\mitch\\Documents\\GitHub\\CSGODatabaseSpring\\output";

    public static synchronized void writeWorkBookToFile(String fileName, Workbook workbook) {
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

    public static boolean isAlreadyExported(String fileName) {
        File file = new File(DIRECTORY_PATH, fileName);
        return file.exists();
    }

    public static boolean isNumber(String cellValue) {
        try {
            Long.parseLong(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String formatNumber(long number) {
        return JspHelper.formatNumber(number).replaceAll(",", ".");
    }
}
