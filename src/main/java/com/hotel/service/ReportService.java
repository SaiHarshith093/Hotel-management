package com.hotel.service;

import com.hotel.dao.ReportDao;
import com.hotel.exception.HotelException;
import com.hotel.model.enums.ReportType;
import com.hotel.model.report.BookingReportRow;
import com.hotel.model.report.FoodSalesReportRow;
import com.hotel.model.report.OccupancyReportRow;
import com.hotel.model.report.RevenueReportRow;
import com.hotel.util.AppConstants;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new HotelException("From date and to date are required.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new HotelException("To date must be on or after from date.");
        }
    }

    public List<RevenueReportRow> getRevenueReport(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return reportDao.findRevenueReport(fromDate, toDate);
    }

    public List<OccupancyReportRow> getOccupancyReport(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return reportDao.findOccupancyReport(fromDate, toDate);
    }

    public List<BookingReportRow> getBookingReport(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return reportDao.findBookingReport(fromDate, toDate);
    }

    public List<FoodSalesReportRow> getFoodSalesReport(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return reportDao.findFoodSalesReport(fromDate, toDate);
    }

    public BigDecimal sumRevenue(List<RevenueReportRow> rows) {
        return rows.stream()
                .map(RevenueReportRow::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal sumFoodSales(List<FoodSalesReportRow> rows) {
        return rows.stream()
                .map(FoodSalesReportRow::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal averageOccupancy(List<OccupancyReportRow> rows) {
        if (rows.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = rows.stream()
                .map(OccupancyReportRow::getOccupancyRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(rows.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    public byte[] exportPdf(ReportType type, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return switch (type) {
            case REVENUE -> buildRevenuePdf(fromDate, toDate);
            case OCCUPANCY -> buildOccupancyPdf(fromDate, toDate);
            case BOOKING -> buildBookingPdf(fromDate, toDate);
            case FOOD_SALES -> buildFoodSalesPdf(fromDate, toDate);
        };
    }

    public byte[] exportExcel(ReportType type, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        return switch (type) {
            case REVENUE -> buildRevenueExcel(fromDate, toDate);
            case OCCUPANCY -> buildOccupancyExcel(fromDate, toDate);
            case BOOKING -> buildBookingExcel(fromDate, toDate);
            case FOOD_SALES -> buildFoodSalesExcel(fromDate, toDate);
        };
    }

    private byte[] buildRevenuePdf(LocalDate fromDate, LocalDate toDate) {
        List<RevenueReportRow> rows = getRevenueReport(fromDate, toDate);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = newDocument(out);
            addReportHeader(document, ReportType.REVENUE, fromDate, toDate);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(12);
            addHeaderRow(table, "Bill ID", "Booking", "Customer", "Room", "Bill Date", "Room ₹", "Food ₹", "Total ₹");

            for (RevenueReportRow row : rows) {
                addRow(table,
                        String.valueOf(row.getBillId()),
                        String.valueOf(row.getBookingId()),
                        row.getCustomerName(),
                        row.getRoomNumber(),
                        row.getBillDate() != null ? row.getBillDate().format(DATETIME_FORMAT) : "",
                        formatMoney(row.getRoomCharges()),
                        formatMoney(row.getFoodCharges()),
                        formatMoney(row.getTotalAmount())
                );
            }
            document.add(table);
            document.add(new Paragraph("Total Revenue: Rs. " + formatMoney(sumRevenue(rows)),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export revenue PDF: " + ex.getMessage());
        }
    }

    private byte[] buildOccupancyPdf(LocalDate fromDate, LocalDate toDate) {
        List<OccupancyReportRow> rows = getOccupancyReport(fromDate, toDate);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = newDocument(out);
            addReportHeader(document, ReportType.OCCUPANCY, fromDate, toDate);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(12);
            addHeaderRow(table, "Date", "Total Rooms", "Occupied", "Occupancy %");

            for (OccupancyReportRow row : rows) {
                addRow(table,
                        row.getReportDate().format(DATE_FORMAT),
                        String.valueOf(row.getTotalRooms()),
                        String.valueOf(row.getOccupiedRooms()),
                        row.getOccupancyRate() + "%"
                );
            }
            document.add(table);
            document.add(new Paragraph("Average Occupancy: " + averageOccupancy(rows) + "%",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export occupancy PDF: " + ex.getMessage());
        }
    }

    private byte[] buildBookingPdf(LocalDate fromDate, LocalDate toDate) {
        List<BookingReportRow> rows = getBookingReport(fromDate, toDate);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = newDocument(out);
            addReportHeader(document, ReportType.BOOKING, fromDate, toDate);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(12);
            addHeaderRow(table, "ID", "Customer", "Room", "Check-In", "Check-Out", "Guests", "Status");

            for (BookingReportRow row : rows) {
                addRow(table,
                        String.valueOf(row.getBookingId()),
                        row.getCustomerName(),
                        row.getRoomNumber(),
                        row.getCheckInDate().format(DATE_FORMAT),
                        row.getCheckOutDate().format(DATE_FORMAT),
                        row.getAdults() + "A/" + row.getChildren() + "C",
                        row.getStatus()
                );
            }
            document.add(table);
            document.add(new Paragraph("Total Bookings: " + rows.size(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export booking PDF: " + ex.getMessage());
        }
    }

    private byte[] buildFoodSalesPdf(LocalDate fromDate, LocalDate toDate) {
        List<FoodSalesReportRow> rows = getFoodSalesReport(fromDate, toDate);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = newDocument(out);
            addReportHeader(document, ReportType.FOOD_SALES, fromDate, toDate);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(12);
            addHeaderRow(table, "Order", "Item", "Customer", "Room", "Qty", "Unit ₹", "Total ₹");

            for (FoodSalesReportRow row : rows) {
                addRow(table,
                        String.valueOf(row.getOrderId()),
                        row.getFoodItemName(),
                        row.getCustomerName(),
                        row.getRoomNumber(),
                        String.valueOf(row.getQuantity()),
                        formatMoney(row.getUnitPrice()),
                        formatMoney(row.getTotalPrice())
                );
            }
            document.add(table);
            document.add(new Paragraph("Total Food Sales: Rs. " + formatMoney(sumFoodSales(rows)),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export food sales PDF: " + ex.getMessage());
        }
    }

    private byte[] buildRevenueExcel(LocalDate fromDate, LocalDate toDate) {
        List<RevenueReportRow> rows = getRevenueReport(fromDate, toDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Revenue");
            CellStyle headerStyle = headerStyle(workbook);
            String[] headers = {"Bill ID", "Booking ID", "Customer", "Room", "Bill Date",
                    "Room Charges", "Food Charges", "Tax", "Total", "Payment Status"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (RevenueReportRow data : rows) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(data.getBillId());
                row.createCell(col++).setCellValue(data.getBookingId());
                row.createCell(col++).setCellValue(data.getCustomerName());
                row.createCell(col++).setCellValue(data.getRoomNumber());
                row.createCell(col++).setCellValue(data.getBillDate() != null
                        ? data.getBillDate().format(DATETIME_FORMAT) : "");
                row.createCell(col++).setCellValue(toDouble(data.getRoomCharges()));
                row.createCell(col++).setCellValue(toDouble(data.getFoodCharges()));
                row.createCell(col++).setCellValue(toDouble(data.getTaxAmount()));
                row.createCell(col++).setCellValue(toDouble(data.getTotalAmount()));
                row.createCell(col).setCellValue(data.getPaymentStatus());
            }
            addSummaryRow(sheet, rowIdx, "Total Revenue", toDouble(sumRevenue(rows)));
            autoSize(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export revenue Excel: " + ex.getMessage());
        }
    }

    private byte[] buildOccupancyExcel(LocalDate fromDate, LocalDate toDate) {
        List<OccupancyReportRow> rows = getOccupancyReport(fromDate, toDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Occupancy");
            CellStyle headerStyle = headerStyle(workbook);
            String[] headers = {"Date", "Total Rooms", "Occupied Rooms", "Occupancy %"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (OccupancyReportRow data : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(data.getReportDate().format(DATE_FORMAT));
                row.createCell(1).setCellValue(data.getTotalRooms());
                row.createCell(2).setCellValue(data.getOccupiedRooms());
                row.createCell(3).setCellValue(data.getOccupancyRate().doubleValue());
            }
            addSummaryRow(sheet, rowIdx, "Average Occupancy %", averageOccupancy(rows).doubleValue());
            autoSize(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export occupancy Excel: " + ex.getMessage());
        }
    }

    private byte[] buildBookingExcel(LocalDate fromDate, LocalDate toDate) {
        List<BookingReportRow> rows = getBookingReport(fromDate, toDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");
            CellStyle headerStyle = headerStyle(workbook);
            String[] headers = {"Booking ID", "Customer", "Room", "Check-In", "Check-Out",
                    "Adults", "Children", "Status"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (BookingReportRow data : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(data.getBookingId());
                row.createCell(1).setCellValue(data.getCustomerName());
                row.createCell(2).setCellValue(data.getRoomNumber());
                row.createCell(3).setCellValue(data.getCheckInDate().format(DATE_FORMAT));
                row.createCell(4).setCellValue(data.getCheckOutDate().format(DATE_FORMAT));
                row.createCell(5).setCellValue(data.getAdults());
                row.createCell(6).setCellValue(data.getChildren());
                row.createCell(7).setCellValue(data.getStatus());
            }
            addSummaryRow(sheet, rowIdx, "Total Bookings", rows.size());
            autoSize(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export booking Excel: " + ex.getMessage());
        }
    }

    private byte[] buildFoodSalesExcel(LocalDate fromDate, LocalDate toDate) {
        List<FoodSalesReportRow> rows = getFoodSalesReport(fromDate, toDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Food Sales");
            CellStyle headerStyle = headerStyle(workbook);
            String[] headers = {"Order ID", "Booking ID", "Customer", "Room", "Item",
                    "Quantity", "Unit Price", "Total", "Status", "Ordered At"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (FoodSalesReportRow data : rows) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(data.getOrderId());
                row.createCell(col++).setCellValue(data.getBookingId());
                row.createCell(col++).setCellValue(data.getCustomerName());
                row.createCell(col++).setCellValue(data.getRoomNumber());
                row.createCell(col++).setCellValue(data.getFoodItemName());
                row.createCell(col++).setCellValue(data.getQuantity());
                row.createCell(col++).setCellValue(toDouble(data.getUnitPrice()));
                row.createCell(col++).setCellValue(toDouble(data.getTotalPrice()));
                row.createCell(col++).setCellValue(data.getOrderStatus());
                row.createCell(col).setCellValue(data.getOrderedAt() != null
                        ? data.getOrderedAt().format(DATETIME_FORMAT) : "");
            }
            addSummaryRow(sheet, rowIdx, "Total Food Sales", toDouble(sumFoodSales(rows)));
            autoSize(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new HotelException("Failed to export food sales Excel: " + ex.getMessage());
        }
    }

    private Document newDocument(ByteArrayOutputStream out) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 48, 48);
        PdfWriter.getInstance(document, out);
        document.open();
        return document;
    }

    private void addReportHeader(Document document, ReportType type, LocalDate from, LocalDate to) throws Exception {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph title = new Paragraph(AppConstants.APP_NAME, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph reportTitle = new Paragraph(type.getDisplayName(), titleFont);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        reportTitle.setSpacingAfter(8);
        document.add(reportTitle);

        Paragraph period = new Paragraph(
                "Period: " + from.format(DATE_FORMAT) + " to " + to.format(DATE_FORMAT), subFont);
        period.setSpacingAfter(12);
        document.add(period);
    }

    private void addHeaderRow(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        headerFont.setColor(new java.awt.Color(255, 255, 255));
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(30, 58, 95));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, String... values) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void addSummaryRow(Sheet sheet, int rowIdx, String label, double value) {
        Row row = sheet.createRow(rowIdx + 1);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private String formatMoney(BigDecimal amount) {
        return amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
    }
}
