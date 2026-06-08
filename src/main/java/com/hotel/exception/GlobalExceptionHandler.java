package com.hotel.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    public String handleCustomerNotFound(CustomerNotFoundException ex,
                                         Model model,
                                         HttpServletResponse response) {
        return notFound(model, response, "Customer Not Found", ex.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public String handleBookingNotFound(BookingNotFoundException ex,
                                        Model model,
                                        HttpServletResponse response) {
        return notFound(model, response, "Booking Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFound(NoHandlerFoundException ex,
                                       Model model,
                                       HttpServletResponse response) {
        return notFound(model, response, "Page Not Found",
                "The page you requested does not exist or has been moved.");
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public String handleRoomNotAvailable(RoomNotAvailableException ex,
                                         Model model,
                                         HttpServletResponse response) {
        return businessError(model, response, HttpStatus.CONFLICT, "Room Not Available", ex.getMessage());
    }

    @ExceptionHandler(BillGenerationException.class)
    public String handleBillGeneration(BillGenerationException ex,
                                       Model model,
                                       HttpServletResponse response) {
        return businessError(model, response, HttpStatus.BAD_REQUEST, "Bill Generation Failed", ex.getMessage());
    }

    @ExceptionHandler(HotelException.class)
    public String handleHotelException(HotelException ex,
                                       Model model,
                                       HttpServletResponse response) {
        return businessError(model, response, HttpStatus.BAD_REQUEST, "Request Could Not Be Completed", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex,
                                         Model model,
                                         HttpServletResponse response,
                                         HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return serverError(model, response, "An unexpected error occurred. Please try again later.");
    }

    private String notFound(Model model, HttpServletResponse response, String title, String message) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        populateModel(model, 404, title, message);
        return "404";
    }

    private String businessError(Model model, HttpServletResponse response, HttpStatus status,
                                 String title, String message) {
        response.setStatus(status.value());
        populateModel(model, status.value(), title, message);
        return "error";
    }

    private String serverError(Model model, HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        populateModel(model, 500, "Internal Server Error", message);
        return "500";
    }

    private void populateModel(Model model, int statusCode, String title, String message) {
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
    }
}
