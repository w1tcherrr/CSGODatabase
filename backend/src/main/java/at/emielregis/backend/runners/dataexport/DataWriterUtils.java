package at.emielregis.backend.runners.dataexport;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DataWriterUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static synchronized void writeWorkBookToFile(String fileName, Workbook workbook) {
        LOGGER.info("Writing file " + fileName);

        SheetBuilder.getBuildersForWorkbook(workbook).forEach(SheetBuilder::build);

        String directoryName = "C:\\Users\\mitch\\Documents\\GitHub\\CSGODatabaseSpring\\output";

        new File(directoryName).mkdir();
        File file = new File(directoryName, fileName);

        try (OutputStream outputWriter = new FileOutputStream(file)) {
            workbook.write(outputWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
