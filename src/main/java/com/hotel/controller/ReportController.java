package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.model.enums.ReportType;
import com.hotel.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String reports(@RequestParam(value = "type", required = false) ReportType type,
                          @RequestParam(value = "fromDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                          @RequestParam(value = "toDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                          Model model) {
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        LocalDate from = fromDate != null ? fromDate : to.minusDays(30);
        ReportType selectedType = type != null ? type : ReportType.REVENUE;

        model.addAttribute("pageTitle", "Reports");
        model.addAttribute("reportTypes", Arrays.asList(ReportType.values()));
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        try {
            switch (selectedType) {
                case REVENUE -> {
                    var rows = reportService.getRevenueReport(from, to);
                    model.addAttribute("revenueRows", rows);
                    model.addAttribute("revenueTotal", reportService.sumRevenue(rows));
                }
                case OCCUPANCY -> {
                    var rows = reportService.getOccupancyReport(from, to);
                    model.addAttribute("occupancyRows", rows);
                    model.addAttribute("avgOccupancy", reportService.averageOccupancy(rows));
                }
                case BOOKING -> {
                    var rows = reportService.getBookingReport(from, to);
                    model.addAttribute("bookingRows", rows);
                    model.addAttribute("bookingCount", rows.size());
                }
                case FOOD_SALES -> {
                    var rows = reportService.getFoodSalesReport(from, to);
                    model.addAttribute("foodSalesRows", rows);
                    model.addAttribute("foodSalesTotal", reportService.sumFoodSales(rows));
                }
            }
        } catch (HotelException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "reports";
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam ReportType type,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        byte[] pdf = reportService.exportPdf(type, fromDate, toDate);
        return fileResponse(pdf, type.name().toLowerCase() + "-report.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam ReportType type,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        byte[] excel = reportService.exportExcel(type, fromDate, toDate);
        return fileResponse(excel, type.name().toLowerCase() + "-report.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] content, String filename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
