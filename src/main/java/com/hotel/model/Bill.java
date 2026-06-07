package com.hotel.model;

import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Booking ID is required")
    @Positive(message = "Booking ID must be positive")
    private Long bookingId;

    @NotNull(message = "Room charges are required")
    @DecimalMin(value = "0.00", message = "Room charges must be zero or greater")
    private BigDecimal roomCharges = BigDecimal.ZERO;

    @NotNull(message = "Food charges are required")
    @DecimalMin(value = "0.00", message = "Food charges must be zero or greater")
    private BigDecimal foodCharges = BigDecimal.ZERO;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.00", message = "Tax amount must be zero or greater")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount must be zero or greater")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount must be zero or greater")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private PaymentMethod paymentMethod;

    private LocalDateTime billDate;

    private LocalDateTime paidAt;

    private String notes;

    public Bill() {
    }

    public Bill(Long id, Long bookingId, BigDecimal roomCharges, BigDecimal foodCharges,
                BigDecimal taxAmount, BigDecimal discountAmount, BigDecimal totalAmount,
                PaymentStatus paymentStatus, PaymentMethod paymentMethod,
                LocalDateTime billDate, LocalDateTime paidAt, String notes) {
        this.id = id;
        this.bookingId = bookingId;
        this.roomCharges = roomCharges;
        this.foodCharges = foodCharges;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.billDate = billDate;
        this.paidAt = paidAt;
        this.notes = notes;
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

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomCharges = roomCharges;
    }

    public BigDecimal getFoodCharges() {
        return foodCharges;
    }

    public void setFoodCharges(BigDecimal foodCharges) {
        this.foodCharges = foodCharges;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", roomCharges=" + roomCharges +
                ", foodCharges=" + foodCharges +
                ", taxAmount=" + taxAmount +
                ", discountAmount=" + discountAmount +
                ", totalAmount=" + totalAmount +
                ", paymentStatus=" + paymentStatus +
                ", paymentMethod=" + paymentMethod +
                ", billDate=" + billDate +
                ", paidAt=" + paidAt +
                ", notes='" + notes + '\'' +
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
        Bill bill = (Bill) o;
        return Objects.equals(id, bill.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
