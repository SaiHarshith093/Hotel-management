package com.hotel.controller;

import com.hotel.exception.HotelException;
import com.hotel.model.Customer;
import com.hotel.model.enums.Gender;
import com.hotel.service.CustomerService;
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
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "search", required = false) String search,
                                Model model) {
        model.addAttribute("pageTitle", "Customer Management");
        model.addAttribute("customers", customerService.findCustomers(search));
        model.addAttribute("search", search);
        return "customers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("pageTitle", "Add Customer");
        model.addAttribute("customer", new Customer());
        addFormOptions(model);
        return "add-customer";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Customer");
        model.addAttribute("customer", customerService.getCustomerById(id));
        addFormOptions(model);
        return "edit-customer";
    }

    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Customer Details");
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customer-details";
    }

    @PostMapping("/add")
    public String addCustomer(@Valid @ModelAttribute("customer") Customer customer,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Customer");
            addFormOptions(model);
            return "add-customer";
        }

        try {
            Customer saved = customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer added successfully.");
            return "redirect:/customers/" + saved.getId();
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Add Customer");
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "add-customer";
        }
    }

    @PostMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id,
                               @Valid @ModelAttribute("customer") Customer customer,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Customer");
            addFormOptions(model);
            return "edit-customer";
        }

        try {
            customerService.updateCustomer(id, customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully.");
            return "redirect:/customers/" + id;
        } catch (HotelException ex) {
            model.addAttribute("pageTitle", "Edit Customer");
            model.addAttribute("errorMessage", ex.getMessage());
            addFormOptions(model);
            return "edit-customer";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully.");
        } catch (HotelException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/customers";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("genders", Gender.values());
    }
}
