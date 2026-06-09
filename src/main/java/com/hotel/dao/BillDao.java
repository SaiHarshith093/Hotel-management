package com.hotel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hotel.model.Bill;
import com.hotel.model.BillView;
import com.hotel.model.enums.PaymentMethod;
import com.hotel.model.enums.PaymentStatus;

@Repository
public class BillDao {

    private static final String BASE_SELECT = """
            SELECT id, booking_id, room_charges, food_charges, tax_amount, discount_amount,
                   total_amount, payment_status, payment_method, bill_date, paid_at, notes
            FROM bills
            """;

    private static final String VIEW_SELECT = """
            SELECT bl.id, bl.booking_id, c.name AS customer_name, r.room_number,
                   b.check_in_date, b.check_out_date, r.price_per_night,
                   bl.room_charges, bl.food_charges, bl.tax_amount, bl.discount_amount,
                   bl.total_amount, bl.payment_status, bl.payment_method,
                   bl.bill_date, bl.paid_at, bl.notes
            FROM bills bl
            INNER JOIN bookings b ON bl.booking_id = b.id
            INNER JOIN customers c ON b.customer_id = c.id
            INNER JOIN rooms r ON b.room_id = r.id
            """;

    private static final RowMapper<Bill> BILL_ROW_MAPPER = (rs, rowNum) -> {
        Bill bill = new Bill();
        bill.setId(rs.getLong("id"));
        bill.setBookingId(rs.getLong("booking_id"));
        bill.setRoomCharges(rs.getBigDecimal("room_charges"));
        bill.setFoodCharges(rs.getBigDecimal("food_charges"));
        bill.setTaxAmount(rs.getBigDecimal("tax_amount"));
        bill.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));

        String paymentMethod = rs.getString("payment_method");
        if (paymentMethod != null) {
            bill.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        }

        Timestamp billDate = rs.getTimestamp("bill_date");
        if (billDate != null) {
            bill.setBillDate(billDate.toLocalDateTime());
        }

        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            bill.setPaidAt(paidAt.toLocalDateTime());
        }

        bill.setNotes(rs.getString("notes"));
        return bill;
    };

    private static final RowMapper<BillView> BILL_VIEW_ROW_MAPPER = (rs, rowNum) -> {
        BillView view = new BillView();
        view.setId(rs.getLong("id"));
        view.setBookingId(rs.getLong("booking_id"));
        view.setCustomerName(rs.getString("customer_name"));
        view.setRoomNumber(rs.getString("room_number"));
        view.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        view.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        view.setPricePerNight(rs.getBigDecimal("price_per_night"));
        long nights = ChronoUnit.DAYS.between(view.getCheckInDate(), view.getCheckOutDate());
        view.setNights(nights);
        view.setRoomCharges(rs.getBigDecimal("room_charges"));
        view.setFoodCharges(rs.getBigDecimal("food_charges"));
        view.setTaxAmount(rs.getBigDecimal("tax_amount"));
        view.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        view.setTotalAmount(rs.getBigDecimal("total_amount"));
        view.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));

        String paymentMethod = rs.getString("payment_method");
        if (paymentMethod != null) {
            view.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        }

        Timestamp billDate = rs.getTimestamp("bill_date");
        if (billDate != null) {
            view.setBillDate(billDate.toLocalDateTime());
        }

        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            view.setPaidAt(paidAt.toLocalDateTime());
        }

        view.setNotes(rs.getString("notes"));
        return view;
    };

    private final JdbcTemplate jdbcTemplate;

    public BillDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BillView> findAll(String search, PaymentStatus paymentStatus) {
        StringBuilder sql = new StringBuilder(VIEW_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                     AND (CAST(bl.id AS CHAR) LIKE ?
                          OR CAST(bl.booking_id AS CHAR) LIKE ?
                          OR c.name LIKE ?
                          OR r.room_number LIKE ?)
                    """);
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (paymentStatus != null) {
            sql.append(" AND bl.payment_status = ?");
            params.add(paymentStatus.name());
        }

        sql.append(" ORDER BY bl.bill_date DESC, bl.id DESC");

        return jdbcTemplate.query(sql.toString(), BILL_VIEW_ROW_MAPPER, params.toArray());
    }

    public Optional<Bill> findById(Long id) {
        try {
            Bill bill = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE id = ?",
                    BILL_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(bill);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<BillView> findViewById(Long id) {
        try {
            BillView view = jdbcTemplate.queryForObject(
                    VIEW_SELECT + " WHERE bl.id = ?",
                    BILL_VIEW_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(view);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Bill> findByBookingId(Long bookingId) {
        try {
            Bill bill = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE booking_id = ?",
                    BILL_ROW_MAPPER,
                    bookingId
            );
            return Optional.ofNullable(bill);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public boolean existsByBookingId(Long bookingId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE booking_id = ?",
                Integer.class,
                bookingId
        );
        return count != null && count > 0;
    }

    public Long save(Bill bill) {
        String sql = """
                INSERT INTO bills (booking_id, room_charges, food_charges, tax_amount, discount_amount,
                                   total_amount, payment_status, payment_method, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, bill.getBookingId());
            ps.setBigDecimal(2, bill.getRoomCharges());
            ps.setBigDecimal(3, bill.getFoodCharges());
            ps.setBigDecimal(4, bill.getTaxAmount());
            ps.setBigDecimal(5, bill.getDiscountAmount());
            ps.setBigDecimal(6, bill.getTotalAmount());
            ps.setString(7, bill.getPaymentStatus().name());
            if (bill.getPaymentMethod() != null) {
                ps.setString(8, bill.getPaymentMethod().name());
            } else {
                ps.setNull(8, java.sql.Types.VARCHAR);
            }
            ps.setString(9, bill.getNotes());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }
    public int receivePayment(Long billId, PaymentMethod paymentMethod) {

    String sql = """
            UPDATE bills
            SET payment_status = 'PAID',
                payment_method = ?,
                paid_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND payment_status = 'PENDING'
            """;

    return jdbcTemplate.update(
            sql,
            paymentMethod.name(),
            billId
    );
}
public java.math.BigDecimal getRevenueToday() {

    java.math.BigDecimal revenue =
            jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM bills
                    WHERE payment_status = 'PAID'
                      AND DATE(paid_at) = CURDATE()
                    """,
                    java.math.BigDecimal.class
            );

    return revenue == null
            ? java.math.BigDecimal.ZERO
            : revenue;
}
}
