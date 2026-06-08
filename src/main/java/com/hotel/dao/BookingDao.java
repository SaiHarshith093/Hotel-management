package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.model.BookingView;
import com.hotel.model.enums.BookingStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingDao {

    private static final String BASE_SELECT = """
            SELECT id, customer_id, room_id, check_in_date, check_out_date, adults, children,
                   status, special_requests, created_by, created_at, updated_at
            FROM bookings
            """;

    private static final String VIEW_SELECT = """
            SELECT b.id, b.customer_id, b.room_id, c.name AS customer_name, r.room_number,
                   b.check_in_date, b.check_out_date, b.status, b.created_at
            FROM bookings b
            INNER JOIN customers c ON b.customer_id = c.id
            INNER JOIN rooms r ON b.room_id = r.id
            """;

    private static final RowMapper<Booking> BOOKING_ROW_MAPPER = (rs, rowNum) -> {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id"));
        booking.setCustomerId(rs.getLong("customer_id"));
        booking.setRoomId(rs.getLong("room_id"));
        booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        booking.setAdults(rs.getInt("adults"));
        booking.setChildren(rs.getInt("children"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setSpecialRequests(rs.getString("special_requests"));
        Long createdBy = rs.getObject("created_by", Long.class);
        booking.setCreatedBy(createdBy);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            booking.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            booking.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return booking;
    };

    private static final RowMapper<BookingView> BOOKING_VIEW_ROW_MAPPER = (rs, rowNum) -> {
        BookingView view = new BookingView();
        view.setId(rs.getLong("id"));
        view.setCustomerId(rs.getLong("customer_id"));
        view.setRoomId(rs.getLong("room_id"));
        view.setCustomerName(rs.getString("customer_name"));
        view.setRoomNumber(rs.getString("room_number"));
        view.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        view.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        view.setStatus(BookingStatus.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            view.setCreatedAt(createdAt.toLocalDateTime());
        }

        return view;
    };

    private final JdbcTemplate jdbcTemplate;

    public BookingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BookingView> findAll(String search, BookingStatus status) {
        StringBuilder sql = new StringBuilder(VIEW_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                     AND (CAST(b.id AS CHAR) LIKE ?
                          OR c.name LIKE ?
                          OR r.room_number LIKE ?)
                    """);
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null) {
            sql.append(" AND b.status = ?");
            params.add(status.name());
        }

        sql.append(" ORDER BY b.check_in_date DESC, b.id DESC");

        return jdbcTemplate.query(sql.toString(), BOOKING_VIEW_ROW_MAPPER, params.toArray());
    }

    public List<BookingView> findActiveForFoodOrder() {
        return jdbcTemplate.query(
                VIEW_SELECT + """
                 WHERE b.status IN (?, ?)
                 ORDER BY b.check_in_date DESC
                """,
                BOOKING_VIEW_ROW_MAPPER,
                BookingStatus.CONFIRMED.name(),
                BookingStatus.CHECKED_IN.name()
        );
    }

    public List<BookingView> findEligibleForBilling() {
        return jdbcTemplate.query(
                VIEW_SELECT + """
                 WHERE b.status IN (?, ?)
                   AND NOT EXISTS (SELECT 1 FROM bills bl WHERE bl.booking_id = b.id)
                 ORDER BY b.check_in_date DESC
                """,
                BOOKING_VIEW_ROW_MAPPER,
                BookingStatus.CHECKED_IN.name(),
                BookingStatus.CHECKED_OUT.name()
        );
    }

    public List<BookingView> findEligibleForCheckout() {
        return jdbcTemplate.query(
                VIEW_SELECT + """
                 WHERE b.status = ?
                   AND NOT EXISTS (SELECT 1 FROM bills bl WHERE bl.booking_id = b.id)
                 ORDER BY b.check_in_date DESC
                """,
                BOOKING_VIEW_ROW_MAPPER,
                BookingStatus.CHECKED_IN.name()
        );
    }

    public Optional<Booking> findById(Long id) {
        try {
            Booking booking = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE id = ?",
                    BOOKING_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(booking);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<BookingView> findViewById(Long id) {
        try {
            BookingView view = jdbcTemplate.queryForObject(
                    VIEW_SELECT + " WHERE b.id = ?",
                    BOOKING_VIEW_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(view);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public int countOverlappingActiveBookings(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM bookings
                WHERE room_id = ?
                  AND status IN (?, ?, ?)
                  AND check_in_date < ?
                  AND check_out_date > ?
                """);

        List<Object> params = new ArrayList<>(List.of(
                roomId,
                BookingStatus.PENDING.name(),
                BookingStatus.CONFIRMED.name(),
                BookingStatus.CHECKED_IN.name(),
                Date.valueOf(checkOut),
                Date.valueOf(checkIn)
        ));

        if (excludeId != null) {
            sql.append(" AND id <> ?");
            params.add(excludeId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public int countActiveBookingsForRoom(Long roomId, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM bookings
                WHERE room_id = ?
                  AND status IN (?, ?, ?)
                """);

        List<Object> params = new ArrayList<>(List.of(
                roomId,
                BookingStatus.PENDING.name(),
                BookingStatus.CONFIRMED.name(),
                BookingStatus.CHECKED_IN.name()
        ));

        if (excludeId != null) {
            sql.append(" AND id <> ?");
            params.add(excludeId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    public Long save(Booking booking) {
        String sql = """
                INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date,
                                      adults, children, status, special_requests, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, booking.getCustomerId());
            ps.setLong(2, booking.getRoomId());
            ps.setDate(3, Date.valueOf(booking.getCheckInDate()));
            ps.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            ps.setInt(5, booking.getAdults());
            ps.setInt(6, booking.getChildren());
            ps.setString(7, booking.getStatus().name());
            ps.setString(8, booking.getSpecialRequests());
            if (booking.getCreatedBy() != null) {
                ps.setLong(9, booking.getCreatedBy());
            } else {
                ps.setNull(9, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int updateStatus(Long id, BookingStatus status) {
        return jdbcTemplate.update(
                "UPDATE bookings SET status = ? WHERE id = ?",
                status.name(),
                id
        );
    }
}
