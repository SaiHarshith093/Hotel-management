package com.hotel.exception;

public class BillGenerationException extends HotelException {

    public BillGenerationException(String message) {
        super(message);
    }

    public BillGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static BillGenerationException forBooking(Long bookingId, String reason) {
        return new BillGenerationException(
                "Unable to generate a bill for booking #" + bookingId + ": " + reason
        );
    }
}
