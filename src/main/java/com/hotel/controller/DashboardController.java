package com.hotel.controller;

import com.hotel.model.DashboardData;
import com.hotel.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(Model model) {
        DashboardData data = dashboardService.getDashboardData();
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("stats", data.getStats());
        model.addAttribute("roomChart", data.getRoomStatusChart());
        model.addAttribute("bookingChart", data.getBookingStatusChart());
        model.addAttribute("revenueChart", data.getRevenueChart());
        return "dashboard";
    }
}
