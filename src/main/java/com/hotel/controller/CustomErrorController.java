package com.hotel.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        model.addAttribute("statusCode", statusCode);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("errorTitle", "Page Not Found");
            model.addAttribute("errorMessage",
                    "The page you are looking for does not exist or has been moved.");
            return "404";
        }

        if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            model.addAttribute("errorTitle", "Internal Server Error");
            model.addAttribute("errorMessage",
                    "Something went wrong on our end. Please try again later.");
            return "500";
        }

        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        model.addAttribute("errorTitle", "Error");
        model.addAttribute("errorMessage",
                message != null ? message.toString() : "An error occurred while processing your request.");
        return "error";
    }
}
