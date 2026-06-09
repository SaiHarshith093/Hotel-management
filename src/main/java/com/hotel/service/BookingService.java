package com.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dao.BookingDao;
import com.hotel.dao.CustomerDao;
import com.hotel.dao.RoomDao;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.CustomerNotFoundException;
import com.hotel.exception.HotelException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.model.Booking;
import com.hotel.model.BookingView;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomStatus;

@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final RoomDao roomDao;
    private final CustomerDao customerDao;

    public BookingService(BookingDao bookingDao, RoomDao roomDao, CustomerDao customerDao) {
        this.bookingDao = bookingDao;
        this.roomDao = roomDao;
        this.customerDao = customerDao;
    }

    public List<BookingView> findBookings(String search, BookingStatus status) {
        return bookingDao.findAll(search, status);
    }

    public Booking getBookingById(Long id) {
        return bookingDao.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        applyDefaults(booking);
        validateBookingDates(booking.getCheckInDate(), booking.getCheckOutDate());

        customerDao.findById(booking.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(booking.getCustomerId()));

        Room room = roomDao.findById(booking.getRoomId())
                .orElseThrow(() -> new HotelException("Room not found with id: " + booking.getRoomId()));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException(room.getRoomNumber());
        }

        validateRoomAvailability(booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate(), null);

        booking.setStatus(BookingStatus.CONFIRMED);

        Long id = bookingDao.save(booking);
        booking.setId(id);

        roomDao.updateStatus(booking.getRoomId(), RoomStatus.RESERVED);

        return booking;
    }

    @Transactional
public void checkIn(Long bookingId) {

    Booking booking = getBookingById(bookingId);

    if (booking.getStatus() != BookingStatus.CONFIRMED) {
        throw new HotelException(
                "Only confirmed bookings can be checked in.");
    }

    bookingDao.updateStatus(
            bookingId,
            BookingStatus.CHECKED_IN);

    roomDao.updateStatus(
            booking.getRoomId(),
            RoomStatus.OCCUPIED);
}

@Transactional
public void checkOut(Long bookingId) {

    Booking booking = getBookingById(bookingId);

    if (booking.getStatus() != BookingStatus.CHECKED_IN) {
        throw new HotelException(
                "Only checked-in bookings can be checked out.");
    }

    bookingDao.updateStatus(
            bookingId,
            BookingStatus.CHECKED_OUT);

    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new HotelException("Booking is already cancelled.");
        }
       if (booking.getStatus() == BookingStatus.CHECKED_IN
        || booking.getStatus() == BookingStatus.CHECKED_OUT
        || booking.getStatus() == BookingStatus.COMPLETED) {

    throw new HotelException(
            "Checked-in or completed bookings cannot be cancelled.");
}

        bookingDao.updateStatus(id, BookingStatus.CANCELLED);

        int remainingActive = bookingDao.countActiveBookingsForRoom(booking.getRoomId(), id);
        if (remainingActive == 0) {
            roomDao.updateStatus(booking.getRoomId(), RoomStatus.AVAILABLE);
        }
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new HotelException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new HotelException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new HotelException("Check-in date cannot be in the past.");
        }
    }

    private void validateRoomAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeId) {
        int overlaps = bookingDao.countOverlappingActiveBookings(roomId, checkIn, checkOut, excludeId);
        if (overlaps > 0) {
            throw RoomNotAvailableException.forDateConflict();
        }
    }

    private void applyDefaults(Booking booking) {
        if (booking.getAdults() == null) {
            booking.setAdults(1);
        }
        if (booking.getChildren() == null) {
            booking.setChildren(0);
        }
    }
}
