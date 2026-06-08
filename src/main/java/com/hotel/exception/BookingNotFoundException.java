package com.hotel.exception;

public class BookingNotFoundException extends HotelException {

    public BookingNotFoundException(Long id) {
        super("Booking #" + id + " was not found. Please verify the booking ID and try again.");
    }
}
