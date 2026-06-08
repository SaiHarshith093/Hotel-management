package com.hotel.dao;

import com.hotel.model.ChartSeries;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardDao {

    private final JdbcTemplate jdbcTemplate;

    public DashboardDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countTotalRooms() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Long.class);
        return count != null ? count : 0L;
    }

    public long countRoomsByStatus(RoomStatus status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rooms WHERE status = ?",
                Long.class,
                status.name()
        );
        return count != null ? count : 0L;
    }

    public long countTotalCustomers() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Long.class);
        return count != null ? count : 0L;
    }

    public long countActiveBookings() {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM bookings
                        WHERE status IN (?, ?, ?)
                        """,
                Long.class,
                BookingStatus.PENDING.name(),
                BookingStatus.CONFIRMED.name(),
                BookingStatus.CHECKED_IN.name()
        );
        return count != null ? count : 0L;
    }

    public BigDecimal sumRevenueToday() {
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(total_amount), 0)
                        FROM bills
                        WHERE payment_status = 'PAID'
                          AND DATE(COALESCE(paid_at, bill_date)) = CURDATE()
                        """,
                BigDecimal.class
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    public ChartSeries findRoomStatusChart() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status, COUNT(*) AS count FROM rooms GROUP BY status ORDER BY status"
        );
        return toChartSeries(rows, "status", "count");
    }

    public ChartSeries findBookingStatusChart() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status, COUNT(*) AS count FROM bookings GROUP BY status ORDER BY status"
        );
        return toChartSeries(rows, "status", "count");
    }

    public ChartSeries findRevenueLast7DaysChart() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                        SELECT DATE(bill_date) AS day, COALESCE(SUM(total_amount), 0) AS revenue
                        FROM bills
                        WHERE bill_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                        GROUP BY DATE(bill_date)
                        ORDER BY day
                        """
        );
        return toChartSeries(rows, "day", "revenue");
    }

    private ChartSeries toChartSeries(List<Map<String, Object>> rows, String labelKey, String valueKey) {
        ChartSeries series = new ChartSeries();
        for (Map<String, Object> row : rows) {
            Object label = row.get(labelKey);
            Object value = row.get(valueKey);
            series.getLabels().add(label != null ? label.toString() : "");
            series.getValues().add(value instanceof Number number ? number : 0);
        }
        return series;
    }
}
