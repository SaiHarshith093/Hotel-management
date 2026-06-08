package com.hotel.dao;

import com.hotel.model.FoodItem;
import com.hotel.model.FoodOrder;
import com.hotel.model.FoodOrderView;
import com.hotel.model.enums.FoodCategory;
import com.hotel.model.enums.FoodOrderStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FoodDao {

    private static final String FOOD_ITEM_SELECT = """
            SELECT id, name, category, price, is_available, description, created_at, updated_at
            FROM food_items
            """;

    private static final String ORDER_VIEW_SELECT = """
            SELECT fo.id, fo.booking_id, fo.food_item_id, fi.name AS food_item_name,
                   c.name AS customer_name, r.room_number,
                   fo.quantity, fo.unit_price, fo.total_price, fo.order_status, fo.notes, fo.ordered_at
            FROM food_orders fo
            INNER JOIN food_items fi ON fo.food_item_id = fi.id
            INNER JOIN bookings b ON fo.booking_id = b.id
            INNER JOIN customers c ON b.customer_id = c.id
            INNER JOIN rooms r ON b.room_id = r.id
            """;

    private static final RowMapper<FoodItem> FOOD_ITEM_ROW_MAPPER = (rs, rowNum) -> {
        FoodItem item = new FoodItem();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setCategory(FoodCategory.valueOf(rs.getString("category")));
        item.setPrice(rs.getBigDecimal("price"));
        item.setAvailable(rs.getBoolean("is_available"));
        item.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return item;
    };

    private static final RowMapper<FoodOrderView> ORDER_VIEW_ROW_MAPPER = (rs, rowNum) -> {
        FoodOrderView view = new FoodOrderView();
        view.setId(rs.getLong("id"));
        view.setBookingId(rs.getLong("booking_id"));
        view.setFoodItemId(rs.getLong("food_item_id"));
        view.setFoodItemName(rs.getString("food_item_name"));
        view.setCustomerName(rs.getString("customer_name"));
        view.setRoomNumber(rs.getString("room_number"));
        view.setQuantity(rs.getInt("quantity"));
        view.setUnitPrice(rs.getBigDecimal("unit_price"));
        view.setTotalPrice(rs.getBigDecimal("total_price"));
        view.setOrderStatus(FoodOrderStatus.valueOf(rs.getString("order_status")));
        view.setNotes(rs.getString("notes"));

        Timestamp orderedAt = rs.getTimestamp("ordered_at");
        if (orderedAt != null) {
            view.setOrderedAt(orderedAt.toLocalDateTime());
        }

        return view;
    };

    private final JdbcTemplate jdbcTemplate;

    public FoodDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FoodItem> findAvailableItems() {
        return jdbcTemplate.query(
                FOOD_ITEM_SELECT + " WHERE is_available = 1 ORDER BY name",
                FOOD_ITEM_ROW_MAPPER
        );
    }

    public Optional<FoodItem> findItemById(Long id) {
        try {
            FoodItem item = jdbcTemplate.queryForObject(
                    FOOD_ITEM_SELECT + " WHERE id = ?",
                    FOOD_ITEM_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<FoodOrderView> findAllOrders(String search) {
        StringBuilder sql = new StringBuilder(ORDER_VIEW_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                     AND (CAST(fo.id AS CHAR) LIKE ?
                          OR fi.name LIKE ?
                          OR c.name LIKE ?
                          OR r.room_number LIKE ?)
                    """);
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY fo.ordered_at DESC, fo.id DESC");

        return jdbcTemplate.query(sql.toString(), ORDER_VIEW_ROW_MAPPER, params.toArray());
    }

    public BigDecimal sumTotalAmount(String search) {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(fo.total_price), 0)
                FROM food_orders fo
                INNER JOIN food_items fi ON fo.food_item_id = fi.id
                INNER JOIN bookings b ON fo.booking_id = b.id
                INNER JOIN customers c ON b.customer_id = c.id
                INNER JOIN rooms r ON b.room_id = r.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                     AND (CAST(fo.id AS CHAR) LIKE ?
                          OR fi.name LIKE ?
                          OR c.name LIKE ?
                          OR r.room_number LIKE ?)
                    """);
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        return jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
    }

    public Optional<FoodOrder> findOrderById(Long id) {
        try {
            FoodOrder order = jdbcTemplate.queryForObject(
                    """
                            SELECT id, booking_id, food_item_id, quantity, unit_price, total_price,
                                   order_status, notes, ordered_at, updated_at
                            FROM food_orders WHERE id = ?
                            """,
                    (rs, rowNum) -> {
                        FoodOrder fo = new FoodOrder();
                        fo.setId(rs.getLong("id"));
                        fo.setBookingId(rs.getLong("booking_id"));
                        fo.setFoodItemId(rs.getLong("food_item_id"));
                        fo.setQuantity(rs.getInt("quantity"));
                        fo.setUnitPrice(rs.getBigDecimal("unit_price"));
                        fo.setTotalPrice(rs.getBigDecimal("total_price"));
                        fo.setOrderStatus(FoodOrderStatus.valueOf(rs.getString("order_status")));
                        fo.setNotes(rs.getString("notes"));

                        Timestamp orderedAt = rs.getTimestamp("ordered_at");
                        if (orderedAt != null) {
                            fo.setOrderedAt(orderedAt.toLocalDateTime());
                        }

                        Timestamp updatedAt = rs.getTimestamp("updated_at");
                        if (updatedAt != null) {
                            fo.setUpdatedAt(updatedAt.toLocalDateTime());
                        }

                        return fo;
                    },
                    id
            );
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Long saveOrder(FoodOrder order) {
        String sql = """
                INSERT INTO food_orders (booking_id, food_item_id, quantity, unit_price, total_price,
                                         order_status, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getBookingId());
            ps.setLong(2, order.getFoodItemId());
            ps.setInt(3, order.getQuantity());
            ps.setBigDecimal(4, order.getUnitPrice());
            ps.setBigDecimal(5, order.getTotalPrice());
            ps.setString(6, order.getOrderStatus().name());
            ps.setString(7, order.getNotes());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int deleteOrderById(Long id) {
        return jdbcTemplate.update("DELETE FROM food_orders WHERE id = ?", id);
    }

    public BigDecimal sumTotalByBookingId(Long bookingId) {
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(total_price), 0)
                        FROM food_orders
                        WHERE booking_id = ? AND order_status <> ?
                        """,
                BigDecimal.class,
                bookingId,
                FoodOrderStatus.CANCELLED.name()
        );
        return total != null ? total : BigDecimal.ZERO;
    }
}
