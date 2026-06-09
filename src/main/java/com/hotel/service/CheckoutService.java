package com.hotel.service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dao.BillDao;
import com.hotel.dao.BookingDao;
import com.hotel.dao.RoomDao;
import com.hotel.exception.BillGenerationException;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.HotelException;
import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.BookingView;
import com.hotel.model.CheckoutPreview;
import com.hotel.model.CheckoutResult;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomStatus;

@Service
public class CheckoutService {

    private final BookingDao bookingDao;
    private final BillDao billDao;
    private final RoomDao roomDao;
    private final BillingService billingService;

    public CheckoutService(BookingDao bookingDao,
                           BillDao billDao,
                           RoomDao roomDao,
                           BillingService billingService) {
        this.bookingDao = bookingDao;
        this.billDao = billDao;
        this.roomDao = roomDao;
        this.billingService = billingService;
    }

    public List<BookingView> getEligibleBookings() {
        return bookingDao.findEligibleForCheckout();
    }

    public CheckoutPreview previewCheckout(Long bookingId) {
        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        validateEligibleForCheckout(booking);

        Room room = roomDao.findById(booking.getRoomId())
                .orElseThrow(() -> new HotelException("Room not found with id: " + booking.getRoomId()));

        BookingView view = bookingDao.findViewById(bookingId)
                .orElseThrow(() -> new HotelException("Booking view not found with id: " + bookingId));

        Bill bill = billingService.buildBill(bookingId, booking, room, null);
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

        CheckoutPreview preview = new CheckoutPreview();
        preview.setBookingId(bookingId);
        preview.setCustomerName(view.getCustomerName());
        preview.setRoomNumber(view.getRoomNumber());
        preview.setCheckInDate(booking.getCheckInDate());
        preview.setCheckOutDate(booking.getCheckOutDate());
        preview.setNights(nights);
        preview.setPricePerNight(room.getPricePerNight());
        preview.setRoomCharges(bill.getRoomCharges());
        preview.setFoodCharges(bill.getFoodCharges());
        preview.setTaxAmount(bill.getTaxAmount());
        preview.setTotalAmount(bill.getTotalAmount());
        return preview;
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckoutResult processCheckout(Long bookingId, String notes) {
        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        validateEligibleForCheckout(booking);

        if (billDao.existsByBookingId(bookingId)) {
            throw BillGenerationException.forBooking(bookingId, "a bill already exists for this booking");
        }

        Room room = roomDao.findById(booking.getRoomId())
                .orElseThrow(() -> new HotelException("Room not found with id: " + booking.getRoomId()));

        Bill bill = billingService.buildBill(bookingId, booking, room, notes);
        Long billId = billDao.save(bill);
        if (billId == null) {
            throw BillGenerationException.forBooking(bookingId, "failed to save the bill");
        }
        bill.setId(billId);

        int bookingUpdated = bookingDao.updateStatus(bookingId, BookingStatus.COMPLETED);
        if (bookingUpdated != 1) {
            throw new HotelException("Failed to update booking status for booking #" + bookingId + ".");
        }

        int remainingActive = bookingDao.countActiveBookingsForRoom(booking.getRoomId(), bookingId);
        if (remainingActive == 0) {
            int roomUpdated = roomDao.updateStatus(booking.getRoomId(), RoomStatus.AVAILABLE);
            if (roomUpdated != 1) {
                throw new HotelException("Failed to update room status for room #" + booking.getRoomId() + ".");
            }
        }

        return new CheckoutResult(bill, bookingId, booking.getRoomId());
    }

    private void validateEligibleForCheckout(Booking booking) {
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new HotelException("Only checked-in bookings can be checked out.");
        }
    }
}
