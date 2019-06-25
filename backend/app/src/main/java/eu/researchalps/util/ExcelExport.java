package eu.researchalps.util;

import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 */
public class ExcelExport {
    public static final String DATE__FORMAT = "dd/mm/yyyy";

    private final HSSFWorkbook workbook = new HSSFWorkbook();
    private HSSFSheet sheet;
    private int rowIndex = 0;
    private Row row;
    private CellStyle dateStyle, headerStyle;
    private int cellIndex = 0;

    public ExcelExport() {
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE__FORMAT));
        headerStyle = workbook.createCellStyle();
        Font f = workbook.createFont();
        f.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerStyle.setFont(f);
    }

    /**
     * @return
     */
    public int getRowIndex() {
        return rowIndex;
    }

    public ExcelExport sheet(String name) {
        sheet = workbook.createSheet(name);
        rowIndex = 0;
        return this;
    }

    private void ensureSheet() {
        if (sheet == null) {
            sheet = workbook.createSheet();
            rowIndex = 0;
        }
    }

    public ExcelExport headers(String[] headerNames) {
        ensureSheet();
        Row header = sheet.createRow(rowIndex++);
        for (int i = 0; i < headerNames.length; i++) {
            final Cell cell = header.createCell(i);
            cell.setCellValue(headerNames[i]);
            cell.setCellStyle(headerStyle);
        }
        return this;
    }

    public ExcelExport row() {
        ensureSheet();
        row = sheet.createRow(rowIndex++);
        cellIndex = 0;
        return this;
    }

    public ExcelExport cell(int index, String value) {
        if (value != null) {
            row.createCell(index).setCellValue(value);
        }
        return this;
    }

    public ExcelExport cell(int index, double value) {
        row.createCell(index).setCellValue(value);
        return this;
    }

    public ExcelExport cell(int index, Date value) {
        if (value != null) {
            final Cell cell = row.createCell(index);
            cell.setCellValue(value);
            cell.setCellStyle(dateStyle);
        }
        return this;
    }

    public ExcelExport linkedCell(int index, String value, String url) {
        if (value != null) {
            Cell cell = row.createCell(index);
            cell.setCellValue(value);
            HSSFHyperlink link = workbook.getCreationHelper().createHyperlink(HSSFHyperlink.LINK_URL);
            link.setAddress(url);
            cell.setHyperlink(link);
        }
        return this;
    }

    public ExcelExport cell(String value) {
        return cell(cellIndex++, value);
    }

    public ExcelExport cell(double value) {
        return cell(cellIndex++, value);
    }

    public ExcelExport cell(Date value) {
        return cell(cellIndex++, value);
    }

    public ExcelExport linkedCell(String value, String link) {
        return linkedCell(cellIndex++, value, link);
    }

    public ExcelExport linkedCell(String link) {
        return linkedCell(cellIndex++, link, link);
    }

    public ExcelExport autoResize() {
        //Auto size all the columns
        for (int x = 0; x < sheet.getRow(0).getPhysicalNumberOfCells(); x++) {
            sheet.autoSizeColumn(x);
        }
        return this;
    }

    public void write(OutputStream out) throws IOException {
        workbook.write(out);
    }

}
