package com.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "expired", required = false) String expired,
                        @RequestParam(value = "denied", required = false) String denied,
                        Model model) {
        model.addAttribute("pageTitle", "Login - Hotel Management");

        if (error != null) {
            model.addAttribute("loginError", true);
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", true);
        }
        if (expired != null) {
            model.addAttribute("sessionExpired", true);
        }
        if (denied != null) {
            model.addAttribute("accessDenied", true);
        }

        return "auth/login";
    }
}
