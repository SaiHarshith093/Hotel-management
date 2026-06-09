package com.hotel.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hotel.exception.HotelException;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.security.HotelUserDetails;
import com.hotel.service.BookingService;
import com.hotel.service.CustomerService;
import com.hotel.service.RoomService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CustomerService customerService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService,
                             CustomerService customerService,
                             RoomService roomService) {
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.roomService = roomService;
    }

    @GetMapping
    public String listBookings(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "status", required = false) BookingStatus status,
                               Model model) {
        model.addAttribute("pageTitle", "Booking Management");
        model.addAttribute("bookings", bookingService.findBookings(search, status));
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("bookingStatuses", BookingStatus.values());
        return "bookings";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("pageTitle", "Create Booking");
        model.addAttribute("booking", new Booking());
        addFormOptions(model);
        return "add-booking";
    }

    @PostMapping("/add")
    public String createBooking(@Valid @ModelAttribute("booking") Booking booking,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal HotelUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Create Booking");
            addFormOptions(model);
            return "add-booking";
        }

        if (userDetails != null) {
            booking.setCreatedBy(userDetails.getId());
        }

        try {
            bookingService.createBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully.");
            return "redirect:/bookings";
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Create Booking");
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "add-booking";
        }
    }

    @PostMapping("/check-in/{id}")
public String checkIn(@PathVariable Long id,
                      RedirectAttributes redirectAttributes) {

    try {

        bookingService.checkIn(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Guest checked in successfully.");

    } catch (HotelException ex) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage());
    }

    return "redirect:/bookings";
}

@PostMapping("/check-out/{id}")
public String checkOut(@PathVariable Long id,
                       RedirectAttributes redirectAttributes) {

    try {

        bookingService.checkOut(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Guest checked out successfully.");

    } catch (HotelException ex) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage());
    }

    return "redirect:/bookings";
}

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("customers", customerService.findCustomers(null));
        List<Room> availableRooms = roomService.findRooms(null, null, RoomStatus.AVAILABLE);
        model.addAttribute("availableRooms", availableRooms);
    }
}
