package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.model.enums.PaymentStatus;
import com.hotel.service.BillingService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bills")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public String listBills(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "status", required = false) PaymentStatus status,
                            Model model) {
        model.addAttribute("pageTitle", "Billing");
        model.addAttribute("bills", billingService.findBills(search, status));
        model.addAttribute("eligibleBookings", billingService.getEligibleBookings());
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        return "bills";
    }

    @GetMapping("/{id}")
    public String viewBill(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Bill Details");
        model.addAttribute("bill", billingService.getBillViewById(id));
        return "bill-details";
    }

    @PostMapping("/generate")
    public String generateBill(@RequestParam Long bookingId,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            var bill = billingService.generateBill(bookingId, notes);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Bill #" + bill.getId() + " generated. Total: Rs. " + bill.getTotalAmount()
            );
            return "redirect:/bills/" + bill.getId();
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bills";
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable Long id) {
        byte[] pdf = billingService.generateBillPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("bill-" + id + ".pdf").build()
        );
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
