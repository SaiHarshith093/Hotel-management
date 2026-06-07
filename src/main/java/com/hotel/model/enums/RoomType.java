package com.hotel.model.enums;

public enum RoomType {
    LUXURY_DOUBLE("Luxury Double"),
    DELUXE_DOUBLE("Deluxe Double"),
    LUXURY_SINGLE("Luxury Single"),
    DELUXE_SINGLE("Deluxe Single");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RoomType fromDisplayName(String displayName) {
        for (RoomType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown room type: " + displayName);
    }
}
