package com.hotel.model;

import com.hotel.model.enums.FoodOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class FoodOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be positive")
    private Long bookingId;

    @NotNull(message = "Food item ID is required")
    @Positive(message = "Food item ID must be positive")
    private Long foodItemId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity = 1;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private FoodOrderStatus orderStatus = FoodOrderStatus.PLACED;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    private LocalDateTime orderedAt;

    private LocalDateTime updatedAt;

    public FoodOrder() {
    }

    public FoodOrder(Long id, Long bookingId, Long foodItemId, Integer quantity, BigDecimal unitPrice,
                     BigDecimal totalPrice, FoodOrderStatus orderStatus, String notes,
                     LocalDateTime orderedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.foodItemId = foodItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.notes = notes;
        this.orderedAt = orderedAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public FoodOrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(FoodOrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FoodOrder{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", foodItemId=" + foodItemId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", orderStatus=" + orderStatus +
                ", notes='" + notes + '\'' +
                ", orderedAt=" + orderedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FoodOrder foodOrder = (FoodOrder) o;
        return Objects.equals(id, foodOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
