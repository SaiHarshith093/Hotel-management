package com.hotel.exception;

public class RoomNotAvailableException extends HotelException {

    public RoomNotAvailableException(String roomNumber) {
        super("Room " + roomNumber + " is not available for the selected dates. Please choose another room.");
    }

    public static RoomNotAvailableException forDateConflict() {
        return new RoomNotAvailableException(
                "The selected room is not available for those dates. Please choose different dates or another room.",
                null
        );
    }

    private RoomNotAvailableException(String message, Object unused) {
        super(message);
    }
}
