package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.service.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public String showCheckoutForm(@RequestParam(value = "bookingId", required = false) Long bookingId,
                                   Model model) {
        model.addAttribute("pageTitle", "Guest Checkout");
        model.addAttribute("eligibleBookings", checkoutService.getEligibleBookings());
        model.addAttribute("selectedBookingId", bookingId);

        if (bookingId != null) {
            try {
                model.addAttribute("preview", checkoutService.previewCheckout(bookingId));
            } catch (HotelException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }

        return "checkout";
    }

    @PostMapping
    public String processCheckout(@RequestParam Long bookingId,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        try {
            var result = checkoutService.processCheckout(bookingId, notes);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Checkout completed for booking #" + result.getBookingId()
                            + ". Bill #" + result.getBill().getId()
                            + " generated. Total: Rs. " + result.getBill().getTotalAmount()
            );
            return "redirect:/bills/" + result.getBill().getId();
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout?bookingId=" + bookingId;
        }
    }
}
