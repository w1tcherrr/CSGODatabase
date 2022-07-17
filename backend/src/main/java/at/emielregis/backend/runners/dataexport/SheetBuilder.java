package at.emielregis.backend.runners.dataexport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SheetBuilder {
    private static final Map<Workbook, List<SheetBuilder>> builders = new ConcurrentHashMap<>();
    private Sheet sheet;
    private Workbook workbook;
    private boolean hasTitleRow = false;
    private boolean hasDescriptionRow = false;
    private final List<Row> rows = new ArrayList<>();

    public static synchronized SheetBuilder create(Workbook workbook, String title) {
        title = title.replaceAll("[/\\\\?*\\[\\]:]", ""); // replaces forbidden characters
        SheetBuilder builder = new SheetBuilder();
        builder.sheet = workbook.createSheet(title);
        builder.workbook = workbook;

        List<SheetBuilder> list = builders.get(workbook);
        if (list == null) {
            builders.put(workbook, new ArrayList<>(List.of(builder)));
        } else {
            list.add(builder);
            builders.put(workbook, list);
        }

        return builder;
    }

    public static synchronized List<SheetBuilder> getBuildersForWorkbook(Workbook workbook) {
        List<SheetBuilder> list = builders.get(workbook);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void setTitleRow(String title) {
        hasTitleRow = true;
        Row titleRow = sheet.createRow(0);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue(title);
        rows.add(0, titleRow);
    }

    public void setDescriptionRow(String... descriptions) {
        hasDescriptionRow = true;
        Row descriptionRow = sheet.createRow(hasTitleRow ? 1 : 0);
        for (int i = 0; i < descriptions.length; i++) {
            Cell cell = descriptionRow.createCell(i);
            cell.setCellValue(descriptions[i]);
        }
        rows.add(hasTitleRow ? 1 : 0, descriptionRow);
    }

    public void addRow(CellStyle[] cellStyles, String... text) {
        Row row = sheet.createRow(rows.size());

        for (int i = 0; i < text.length; i++) {
            Cell cell = row.createCell(i);

            // set the cell style if specified
            if (cellStyles != null && i < cellStyles.length && cellStyles[i] != null) {
                cell.setCellStyle(cellStyles[i]);
            }

            String value = text[i];
            if (value != null && isNumber(value)) {
                long longValue = Long.parseLong(value);
                if (longValue != 0) {
                    cell.setCellValue(longValue);
                } else {
                    cell.setCellValue("");
                }
            } else {
                cell.setCellValue(value != null ? value : "");
            }
        }
        rows.add(row);
    }

    private static boolean isNumber(String cellValue) {
        try {
            Long.parseLong(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void emptyLines(int l) {
        for (int i = 0; i < l; i++) {
            addRow(null, "");
        }
    }

    public void build() {
        setRowNumbers();
        style();
    }

    public void style() {
        if (hasTitleRow) {
            Row titleRow = rows.get(0);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Serif");
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            titleStyle.setFont(font);

            setRowStyle(titleRow, titleStyle);
        }

        if (hasDescriptionRow) {
            Row descriptionRow = rows.get(hasTitleRow ? 1 : 0);

            CellStyle descriptionStyle = workbook.createCellStyle();
            descriptionStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            descriptionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Serif");
            font.setFontHeightInPoints((short) 12);
            descriptionStyle.setFont(font);

            setRowStyle(descriptionRow, descriptionStyle);
        }

        CellStyle standardStyle = workbook.createCellStyle();
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Serif");
        font.setFontHeightInPoints((short) 11);
        standardStyle.setFont(font);

        for (Row row : rows) {
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell == null) continue;
                if (cell.getCellStyle() == null) {
                    cell.setCellStyle(standardStyle);
                }
            }
        }

        // auto size all columns
        for (int i = 0; i <= getLastValidColumn(); i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) > 768) {
                sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.1));
            } else {
                sheet.setColumnWidth(i, 768);
            }
        }
    }

    private int getLastValidColumn() {
        int lastValidColumn = 0;
        for (Row row : rows) {
            for (int i = 0; i < 20; i++) {
                Cell cell = row.getCell(i);
                if (cell == null) {
                    continue;
                }
                if (cell.getCellType() == CellType.STRING) {
                    if (!StringUtils.isBlank(cell.getStringCellValue())) {
                        if (i > lastValidColumn) {
                            lastValidColumn = i;
                        }
                    }
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    int intValue = (int) cell.getNumericCellValue();
                    if (intValue != 0) {
                        if (i > lastValidColumn) {
                            lastValidColumn = i;
                        }
                    }
                }
            }
        }
        return lastValidColumn;
    }

    private void setRowStyle(Row row, CellStyle style) {
        row.cellIterator().forEachRemaining(cell -> cell.setCellStyle(style));
        row.setRowStyle(style);
    }

    private void setRowNumbers() {
        int i = 0;
        for (Row row : rows) {
            row.setRowNum(i++);
        }
    }
}
