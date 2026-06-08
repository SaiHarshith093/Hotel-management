package com.hotel.model.enums;

public enum ReportType {
    REVENUE("Revenue Report"),
    OCCUPANCY("Occupancy Report"),
    BOOKING("Booking Report"),
    FOOD_SALES("Food Sales Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
