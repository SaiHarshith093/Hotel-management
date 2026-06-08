package com.hotel.service;

import com.hotel.dao.BookingDao;
import com.hotel.dao.FoodDao;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.HotelException;
import com.hotel.model.Booking;
import com.hotel.model.BookingView;
import com.hotel.model.FoodItem;
import com.hotel.model.FoodOrder;
import com.hotel.model.FoodOrderView;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.FoodOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FoodService {

    private final FoodDao foodDao;
    private final BookingDao bookingDao;

    public FoodService(FoodDao foodDao, BookingDao bookingDao) {
        this.foodDao = foodDao;
        this.bookingDao = bookingDao;
    }

    public List<FoodOrderView> findOrders(String search) {
        return foodDao.findAllOrders(search);
    }

    public BigDecimal calculateTotalFoodAmount(String search) {
        return foodDao.sumTotalAmount(search);
    }

    public BigDecimal calculateOrderAmount(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public List<FoodItem> getAvailableMenuItems() {
        return foodDao.findAvailableItems();
    }

    public List<BookingView> getActiveBookings() {
        return bookingDao.findActiveForFoodOrder();
    }

    @Transactional
    public FoodOrder createOrder(FoodOrder order) {
        Booking booking = bookingDao.findById(order.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(order.getBookingId()));

        if (booking.getStatus() != BookingStatus.CONFIRMED
                && booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new HotelException("Food orders can only be placed for confirmed or checked-in bookings.");
        }

        FoodItem foodItem = foodDao.findItemById(order.getFoodItemId())
                .orElseThrow(() -> new HotelException("Food item not found with id: " + order.getFoodItemId()));

        if (!foodItem.isAvailable()) {
            throw new HotelException("Food item is not available: " + foodItem.getName());
        }

        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new HotelException("Quantity must be greater than zero.");
        }

        order.setUnitPrice(foodItem.getPrice());
        order.setTotalPrice(calculateOrderAmount(foodItem.getPrice(), order.getQuantity()));
        order.setOrderStatus(FoodOrderStatus.PLACED);

        Long id = foodDao.saveOrder(order);
        order.setId(id);
        return order;
    }

    @Transactional
    public void deleteOrder(Long id) {
        foodDao.findOrderById(id)
                .orElseThrow(() -> new HotelException("Food order not found with id: " + id));

        int deleted = foodDao.deleteOrderById(id);
        if (deleted == 0) {
            throw new HotelException("Food order not found with id: " + id);
        }
    }
}
