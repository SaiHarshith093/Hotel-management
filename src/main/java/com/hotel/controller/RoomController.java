package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "roomType", required = false) RoomType roomType,
                            @RequestParam(value = "status", required = false) RoomStatus status,
                            Model model) {
        model.addAttribute("pageTitle", "Room Management");
        model.addAttribute("rooms", roomService.findRooms(search, roomType, status));
        model.addAttribute("search", search);
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedStatus", status);
        addFormOptions(model);
        return "rooms";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("pageTitle", "Add Room");
        model.addAttribute("room", new Room());
        model.addAttribute("isEdit", false);
        addFormOptions(model);
        return "add-room";
    }

    @PostMapping("/add")
    public String addRoom(@Valid @ModelAttribute("room") Room room,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Room");
            model.addAttribute("isEdit", false);
            addFormOptions(model);
            return "add-room";
        }

        try {
            roomService.createRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Room added successfully.");
            return "redirect:/rooms";
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Add Room");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "add-room";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Room");
        model.addAttribute("room", roomService.getRoomById(id));
        model.addAttribute("isEdit", true);
        addFormOptions(model);
        return "edit-room";
    }

    @PostMapping("/edit/{id}")
    public String editRoom(@PathVariable Long id,
                           @Valid @ModelAttribute("room") Room room,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Room");
            model.addAttribute("isEdit", true);
            addFormOptions(model);
            return "edit-room";
        }

        try {
            roomService.updateRoom(id, room);
            redirectAttributes.addFlashAttribute("successMessage", "Room updated successfully.");
            return "redirect:/rooms";
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Edit Room");
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "edit-room";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully.");
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/rooms";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
    }
}
