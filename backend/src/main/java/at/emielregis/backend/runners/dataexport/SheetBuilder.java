package at.emielregis.backend.runners.dataexport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for building and managing Excel sheets using Apache POI.
 * Provides functionality to create sheets, add rows, style content, and write data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SheetBuilder {
    private static final Map<Workbook, List<SheetBuilder>> builders = new ConcurrentHashMap<>();
    private Sheet sheet;
    private Workbook workbook;
    private boolean hasTitleRow = false;
    private boolean hasDescriptionRow = false;
    private final List<Row> rows = new ArrayList<>();

    /**
     * Creates a new sheet builder for a specific workbook and sheet title.
     *
     * @param workbook The workbook to add the sheet to.
     * @param title    The title of the sheet (invalid characters will be removed).
     * @return A new instance of `SheetBuilder`.
     */
    public static synchronized SheetBuilder create(Workbook workbook, String title) {
        title = title.replaceAll("[/\\\\?*\\[\\]:]", ""); // Remove forbidden characters in sheet names
        SheetBuilder builder = new SheetBuilder();
        builder.sheet = workbook.createSheet(title);
        builder.workbook = workbook;

        // Add the builder to the map of builders for this workbook
        builders.computeIfAbsent(workbook, k -> new ArrayList<>()).add(builder);

        return builder;
    }

    /**
     * Retrieves all sheet builders associated with a specific workbook.
     *
     * @param workbook The workbook to retrieve builders for.
     * @return A list of `SheetBuilder` instances.
     */
    public static synchronized List<SheetBuilder> getBuildersForWorkbook(Workbook workbook) {
        return builders.getOrDefault(workbook, new ArrayList<>());
    }

    /**
     * Sets the title row of the sheet.
     *
     * @param title The title text.
     */
    public void setTitleRow(String title) {
        hasTitleRow = true;
        Row titleRow = sheet.createRow(0);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue(title);
        rows.add(0, titleRow);
    }

    /**
     * Sets the description row with column headers.
     *
     * @param descriptions The column descriptions.
     */
    public void setDescriptionRow(String... descriptions) {
        hasDescriptionRow = true;
        Row descriptionRow = sheet.createRow(hasTitleRow ? 1 : 0);
        for (int i = 0; i < descriptions.length; i++) {
            Cell cell = descriptionRow.createCell(i);
            cell.setCellValue(descriptions[i]);
        }
        rows.add(hasTitleRow ? 1 : 0, descriptionRow);
    }

    /**
     * Adds a row to the sheet with optional cell styles.
     *
     * @param cellStyles Styles for each cell in the row (nullable).
     * @param text       The cell values for the row.
     */
    public void addRow(CellStyle[] cellStyles, String... text) {
        Row row = sheet.createRow(rows.size());

        for (int i = 0; i < text.length; i++) {
            Cell cell = row.createCell(i);

            // Apply the provided cell style if available
            if (cellStyles != null && i < cellStyles.length && cellStyles[i] != null) {
                cell.setCellStyle(cellStyles[i]);
            }

            // Determine the value type and format accordingly
            String value = text[i];
            if (value != null && isIntegerOrLong(value)) {
                long longValue = Long.parseLong(value);
                cell.setCellValue(longValue != 0 ? NumberFormat.getNumberInstance(Locale.GERMAN).format(longValue) : "");
            } else if (value != null && isDouble(value)) {
                double doubleValue = Double.parseDouble(value);
                cell.setCellValue(doubleValue != 0 ? String.format(Locale.GERMAN, "%1$,.2f", doubleValue) : "");
            } else {
                cell.setCellValue(value != null ? value : "");
            }
        }
        rows.add(row);
    }

    /**
     * Adds a specified number of empty rows to the sheet.
     *
     * @param l The number of empty rows to add.
     */
    public void emptyLines(int l) {
        for (int i = 0; i < l; i++) {
            addRow(null, "");
        }
    }

    /**
     * Finalizes the sheet by applying styles and row numbering.
     */
    public void build() {
        setRowNumbers();
        style();
    }

    /**
     * Applies styles to the title row, description row, and other cells.
     * Also adjusts column widths.
     */
    public void style() {
        if (hasTitleRow) {
            Row titleRow = rows.get(0);
            CellStyle titleStyle = createCellStyle(IndexedColors.LIGHT_BLUE, (short) 16, true);
            setRowStyle(titleRow, titleStyle);
        }

        if (hasDescriptionRow) {
            Row descriptionRow = rows.get(hasTitleRow ? 1 : 0);
            CellStyle descriptionStyle = createCellStyle(IndexedColors.LIGHT_CORNFLOWER_BLUE, (short) 12, false);
            setRowStyle(descriptionRow, descriptionStyle);
        }

        CellStyle standardStyle = createCellStyle(null, (short) 11, false);

        for (Row row : rows) {
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellStyle() == null) {
                    cell.setCellStyle(standardStyle);
                }
            }
        }

        adjustColumnWidths();
    }

    /**
     * Adjusts the widths of all columns based on their content.
     */
    private void adjustColumnWidths() {
        for (int i = 0; i <= getLastValidColumn(); i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) > 768) {
                sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.1));
            } else {
                sheet.setColumnWidth(i, 768);
            }
        }
    }

    /**
     * Determines the last column with valid data.
     *
     * @return The index of the last valid column.
     */
    private int getLastValidColumn() {
        int lastValidColumn = 0;
        for (Row row : rows) {
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cellHasValue(cell)) {
                    lastValidColumn = Math.max(lastValidColumn, i);
                }
            }
        }
        return lastValidColumn;
    }

    /**
     * Checks if a cell contains a valid value.
     *
     * @param cell The cell to check.
     * @return {@code true} if the cell contains a value; otherwise {@code false}.
     */
    private boolean cellHasValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> !StringUtils.isBlank(cell.getStringCellValue());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> false;
        };
    }

    /**
     * Creates a cell style with specified attributes.
     *
     * @param color      The fill color (nullable).
     * @param fontSize   The font size.
     * @param isBold     Whether the font is bold.
     * @return The created {@link CellStyle}.
     */
    private CellStyle createCellStyle(IndexedColors color, short fontSize, boolean isBold) {
        CellStyle style = workbook.createCellStyle();
        if (color != null) {
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Serif");
        font.setFontHeightInPoints(fontSize);
        font.setBold(isBold);
        style.setFont(font);
        return style;
    }

    /**
     * Applies a style to all cells in a row.
     *
     * @param row  The row to style.
     * @param style The style to apply.
     */
    private void setRowStyle(Row row, CellStyle style) {
        row.cellIterator().forEachRemaining(cell -> cell.setCellStyle(style));
        row.setRowStyle(style);
    }

    /**
     * Sets row numbers for all rows.
     */
    private void setRowNumbers() {
        int i = 0;
        for (Row row : rows) {
            row.setRowNum(i++);
        }
    }

    private static boolean isIntegerOrLong(String cellValue) {
        try {
            Long.parseLong(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String cellValue) {
        try {
            Double.parseDouble(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
