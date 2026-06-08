package com.hotel.dao;

import com.hotel.model.report.BookingReportRow;
import com.hotel.model.report.FoodSalesReportRow;
import com.hotel.model.report.OccupancyReportRow;
import com.hotel.model.report.RevenueReportRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ReportDao {

    private static final RowMapper<RevenueReportRow> REVENUE_ROW_MAPPER = (rs, rowNum) -> {
        RevenueReportRow row = new RevenueReportRow();
        row.setBillId(rs.getLong("bill_id"));
        row.setBookingId(rs.getLong("booking_id"));
        row.setCustomerName(rs.getString("customer_name"));
        row.setRoomNumber(rs.getString("room_number"));
        Timestamp billDate = rs.getTimestamp("bill_date");
        if (billDate != null) {
            row.setBillDate(billDate.toLocalDateTime());
        }
        row.setRoomCharges(rs.getBigDecimal("room_charges"));
        row.setFoodCharges(rs.getBigDecimal("food_charges"));
        row.setTaxAmount(rs.getBigDecimal("tax_amount"));
        row.setTotalAmount(rs.getBigDecimal("total_amount"));
        row.setPaymentStatus(rs.getString("payment_status"));
        return row;
    };

    private static final RowMapper<OccupancyReportRow> OCCUPANCY_ROW_MAPPER = (rs, rowNum) -> {
        OccupancyReportRow row = new OccupancyReportRow();
        row.setReportDate(rs.getDate("report_date").toLocalDate());
        row.setTotalRooms(rs.getLong("total_rooms"));
        row.setOccupiedRooms(rs.getLong("occupied_rooms"));
        long total = row.getTotalRooms();
        long occupied = row.getOccupiedRooms();
        BigDecimal rate = total > 0
                ? BigDecimal.valueOf(occupied * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        row.setOccupancyRate(rate);
        return row;
    };

    private static final RowMapper<BookingReportRow> BOOKING_ROW_MAPPER = (rs, rowNum) -> {
        BookingReportRow row = new BookingReportRow();
        row.setBookingId(rs.getLong("booking_id"));
        row.setCustomerName(rs.getString("customer_name"));
        row.setRoomNumber(rs.getString("room_number"));
        row.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        row.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        row.setAdults(rs.getInt("adults"));
        row.setChildren(rs.getInt("children"));
        row.setStatus(rs.getString("status"));
        return row;
    };

    private static final RowMapper<FoodSalesReportRow> FOOD_SALES_ROW_MAPPER = (rs, rowNum) -> {
        FoodSalesReportRow row = new FoodSalesReportRow();
        row.setOrderId(rs.getLong("order_id"));
        row.setBookingId(rs.getLong("booking_id"));
        row.setCustomerName(rs.getString("customer_name"));
        row.setRoomNumber(rs.getString("room_number"));
        row.setFoodItemName(rs.getString("food_item_name"));
        row.setQuantity(rs.getInt("quantity"));
        row.setUnitPrice(rs.getBigDecimal("unit_price"));
        row.setTotalPrice(rs.getBigDecimal("total_price"));
        row.setOrderStatus(rs.getString("order_status"));
        Timestamp orderedAt = rs.getTimestamp("ordered_at");
        if (orderedAt != null) {
            row.setOrderedAt(orderedAt.toLocalDateTime());
        }
        return row;
    };

    private final JdbcTemplate jdbcTemplate;

    public ReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RevenueReportRow> findRevenueReport(LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(
                """
                        SELECT bl.id AS bill_id, bl.booking_id, c.name AS customer_name, r.room_number,
                               bl.bill_date, bl.room_charges, bl.food_charges, bl.tax_amount,
                               bl.total_amount, bl.payment_status
                        FROM bills bl
                        INNER JOIN bookings b ON bl.booking_id = b.id
                        INNER JOIN customers c ON b.customer_id = c.id
                        INNER JOIN rooms r ON b.room_id = r.id
                        WHERE DATE(bl.bill_date) BETWEEN ? AND ?
                        ORDER BY bl.bill_date DESC, bl.id DESC
                        """,
                REVENUE_ROW_MAPPER,
                Date.valueOf(fromDate),
                Date.valueOf(toDate)
        );
    }

    public List<OccupancyReportRow> findOccupancyReport(LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(
                """
                        WITH RECURSIVE dates AS (
                            SELECT ? AS report_date
                            UNION ALL
                            SELECT report_date + INTERVAL 1 DAY
                            FROM dates
                            WHERE report_date + INTERVAL 1 DAY <= ?
                        )
                        SELECT d.report_date,
                               (SELECT COUNT(*) FROM rooms) AS total_rooms,
                               (SELECT COUNT(*)
                                FROM bookings b
                                WHERE b.status NOT IN ('CANCELLED')
                                  AND b.check_in_date <= d.report_date
                                  AND b.check_out_date > d.report_date) AS occupied_rooms
                        FROM dates d
                        ORDER BY d.report_date
                        """,
                OCCUPANCY_ROW_MAPPER,
                Date.valueOf(fromDate),
                Date.valueOf(toDate)
        );
    }

    public List<BookingReportRow> findBookingReport(LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(
                """
                        SELECT b.id AS booking_id, c.name AS customer_name, r.room_number,
                               b.check_in_date, b.check_out_date, b.adults, b.children, b.status
                        FROM bookings b
                        INNER JOIN customers c ON b.customer_id = c.id
                        INNER JOIN rooms r ON b.room_id = r.id
                        WHERE b.check_in_date BETWEEN ? AND ?
                        ORDER BY b.check_in_date DESC, b.id DESC
                        """,
                BOOKING_ROW_MAPPER,
                Date.valueOf(fromDate),
                Date.valueOf(toDate)
        );
    }

    public List<FoodSalesReportRow> findFoodSalesReport(LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.query(
                """
                        SELECT fo.id AS order_id, fo.booking_id, c.name AS customer_name, r.room_number,
                               fi.name AS food_item_name, fo.quantity, fo.unit_price, fo.total_price,
                               fo.order_status, fo.ordered_at
                        FROM food_orders fo
                        INNER JOIN food_items fi ON fo.food_item_id = fi.id
                        INNER JOIN bookings b ON fo.booking_id = b.id
                        INNER JOIN customers c ON b.customer_id = c.id
                        INNER JOIN rooms r ON b.room_id = r.id
                        WHERE DATE(fo.ordered_at) BETWEEN ? AND ?
                          AND fo.order_status <> 'CANCELLED'
                        ORDER BY fo.ordered_at DESC, fo.id DESC
                        """,
                FOOD_SALES_ROW_MAPPER,
                Date.valueOf(fromDate),
                Date.valueOf(toDate)
        );
    }
}
