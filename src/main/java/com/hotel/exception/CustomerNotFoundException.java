package com.hotel.exception;

public class CustomerNotFoundException extends HotelException {

    public CustomerNotFoundException(Long id) {
        super("Customer #" + id + " was not found. The record may have been removed or the ID is incorrect.");
    }
}
