package com.hotel.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dao.BillDao;
import com.hotel.dao.BookingDao;
import com.hotel.dao.FoodDao;
import com.hotel.dao.RoomDao;
import com.hotel.exception.BillGenerationException;
import com.hotel.exception.BookingNotFoundException;
import com.hotel.exception.HotelException;
import com.hotel.model.Bill;
import com.hotel.model.BillView;
import com.hotel.model.Booking;
import com.hotel.model.BookingView;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.PaymentStatus;
import com.hotel.model.enums.RoomStatus;
import com.hotel.util.AppConstants;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class BillingService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final BillDao billDao;
    private final BookingDao bookingDao;
    private final RoomDao roomDao;
    private final FoodDao foodDao;

    public BillingService(BillDao billDao,
                          BookingDao bookingDao,
                          RoomDao roomDao,
                          FoodDao foodDao) {
        this.billDao = billDao;
        this.bookingDao = bookingDao;
        this.roomDao = roomDao;
        this.foodDao = foodDao;
    }

    public List<BillView> findBills(String search, PaymentStatus paymentStatus) {
        return billDao.findAll(search, paymentStatus);
    }

    public BillView getBillViewById(Long id) {
        return billDao.findViewById(id)
                .orElseThrow(() -> new HotelException("Bill not found with id: " + id));
    }

    public List<BookingView> getEligibleBookings() {
        return bookingDao.findEligibleForBilling();
    }

    @Transactional
    public Bill generateBill(Long bookingId, String notes) {
        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.CHECKED_IN
                && booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw BillGenerationException.forBooking(bookingId,
                    "bill can only be generated for checked-in or checked-out bookings");
        }

        if (billDao.existsByBookingId(bookingId)) {
            throw BillGenerationException.forBooking(bookingId, "a bill already exists for this booking");
        }

        Room room = roomDao.findById(booking.getRoomId())
                .orElseThrow(() -> new HotelException("Room not found with id: " + booking.getRoomId()));

        Bill bill = buildBill(bookingId, booking, room, notes);
        Long id = billDao.save(bill);
        bill.setId(id);
        return bill;
    }

    public byte[] generateBillPdf(Long billId) {
        BillView bill = getBillViewById(billId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            Paragraph title = new Paragraph(AppConstants.APP_NAME, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph subtitle = new Paragraph("Tax Invoice / Bill", headingFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            document.add(new Paragraph("Bill ID: #" + bill.getId(), normalFont));
            document.add(new Paragraph("Booking ID: #" + bill.getBookingId(), normalFont));
            document.add(new Paragraph("Customer: " + bill.getCustomerName(), normalFont));
            document.add(new Paragraph("Room: " + bill.getRoomNumber(), normalFont));
            document.add(new Paragraph(
                    "Stay: " + bill.getCheckInDate().format(DATE_FORMAT)
                            + " to " + bill.getCheckOutDate().format(DATE_FORMAT)
                            + " (" + bill.getNights() + " night(s))",
                    normalFont
            ));
            if (bill.getBillDate() != null) {
                document.add(new Paragraph("Bill Date: " + bill.getBillDate().format(DATETIME_FORMAT), normalFont));
            }
            Paragraph spacer = new Paragraph(" ");
            spacer.setSpacingAfter(10);
            document.add(spacer);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            addTableRow(table, "Room Charges (" + bill.getNights() + " x "
                    + formatCurrency(bill.getPricePerNight()) + ")", formatCurrency(bill.getRoomCharges()), normalFont);
            addTableRow(table, "Food Charges", formatCurrency(bill.getFoodCharges()), normalFont);
            addTableRow(table, "GST (18%)", formatCurrency(bill.getTaxAmount()), normalFont);
            addTableRow(table, "Total Amount", formatCurrency(bill.getTotalAmount()), boldFont);

            document.add(table);

            document.add(new Paragraph("Payment Status: " + bill.getPaymentStatus().name(), normalFont));
            if (bill.getPaymentMethod() != null) {
                document.add(new Paragraph("Payment Method: " + bill.getPaymentMethod().name(), normalFont));
            }
            if (bill.getNotes() != null && !bill.getNotes().isBlank()) {
                Paragraph notesSpacer = new Paragraph(" ");
                notesSpacer.setSpacingBefore(10);
                document.add(notesSpacer);
                document.add(new Paragraph("Notes: " + bill.getNotes(), normalFont));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new BillGenerationException("Failed to generate bill PDF: " + ex.getMessage(), ex);
        }
    }

    public Bill buildBill(Long bookingId, Booking booking, Room room, String notes) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (nights <= 0) {
            throw BillGenerationException.forBooking(bookingId, "invalid booking dates for billing");
        }

        BigDecimal roomCharges = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal foodCharges = foodDao.sumTotalByBookingId(bookingId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = roomCharges.add(foodCharges);
        BigDecimal taxAmount = subtotal.multiply(AppConstants.GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill();
        bill.setBookingId(bookingId);
        bill.setRoomCharges(roomCharges);
        bill.setFoodCharges(foodCharges);
        bill.setTaxAmount(taxAmount);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTotalAmount(totalAmount);
        bill.setPaymentStatus(PaymentStatus.PENDING);
        bill.setNotes(notes);
        return bill;
    }

    private void addTableRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPaddingBottom(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatCurrency(BigDecimal amount) {
        return "Rs. " + amount.setScale(2, RoundingMode.HALF_UP);
    }
    @Transactional
public void receivePayment(Long billId,
                           PaymentMethod paymentMethod) {

    Bill bill = billDao.findById(billId)
            .orElseThrow(() ->
                    new HotelException("Bill not found."));

    if (bill.getPaymentStatus() != PaymentStatus.PENDING) {
        throw new HotelException(
                "Payment has already been processed.");
    }

    int updated = billDao.receivePayment(
            billId,
            paymentMethod);

    if (updated != 1) {
        throw new HotelException(
                "Failed to update payment.");
    }

Booking booking = bookingDao.findById(
        bill.getBookingId())
        .orElseThrow(() ->
                new HotelException("Booking not found."));

bookingDao.updateStatus(
        booking.getId(),
        BookingStatus.COMPLETED);

roomDao.updateStatus(
        booking.getRoomId(),
        RoomStatus.AVAILABLE);
}
}
