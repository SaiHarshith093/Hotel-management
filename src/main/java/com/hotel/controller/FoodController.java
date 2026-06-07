package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.model.FoodOrder;
import com.hotel.service.FoodService;
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
@RequestMapping("/food-orders")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("pageTitle", "Food Orders");
        model.addAttribute("orders", foodService.findOrders(search));
        model.addAttribute("totalFoodAmount", foodService.calculateTotalFoodAmount(search));
        model.addAttribute("search", search);
        return "food-orders";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("pageTitle", "Add Food Order");
        model.addAttribute("foodOrder", new FoodOrder());
        addFormOptions(model);
        return "add-food-order";
    }

    @PostMapping("/add")
    public String addOrder(@Valid @ModelAttribute("foodOrder") FoodOrder foodOrder,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Food Order");
            addFormOptions(model);
            return "add-food-order";
        }

        try {
            FoodOrder saved = foodService.createOrder(foodOrder);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Food order #" + saved.getId() + " placed. Amount: ₹" + saved.getTotalPrice()
            );
            return "redirect:/food-orders";
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Add Food Order");
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "add-food-order";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            foodService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Food order deleted successfully.");
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/food-orders";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("menuItems", foodService.getAvailableMenuItems());
        model.addAttribute("activeBookings", foodService.getActiveBookings());
    }
}
