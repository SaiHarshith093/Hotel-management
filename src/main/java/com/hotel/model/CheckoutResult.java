package com.hotel.model;

import java.io.Serializable;

public class CheckoutResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Bill bill;
    private final Long bookingId;
    private final Long roomId;

    public CheckoutResult(Bill bill, Long bookingId, Long roomId) {
        this.bill = bill;
        this.bookingId = bookingId;
        this.roomId = roomId;
    }

    public Bill getBill() {
        return bill;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getRoomId() {
        return roomId;
    }
}
