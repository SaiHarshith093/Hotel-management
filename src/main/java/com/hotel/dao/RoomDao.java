package com.hotel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;

@Repository
public class RoomDao {

    private static final String BASE_SELECT = """
            SELECT id, room_number, room_type, price_per_night, floor_number, max_occupancy,
                   status, description, created_at, updated_at
            FROM rooms
            """;

    private static final RowMapper<Room> ROOM_ROW_MAPPER = (rs, rowNum) -> {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(RoomType.fromDisplayName(rs.getString("room_type")));
        room.setPricePerNight(rs.getBigDecimal("price_per_night"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setMaxOccupancy(rs.getInt("max_occupancy"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        room.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            room.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            room.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return room;
    };

    private final JdbcTemplate jdbcTemplate;

    public RoomDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Room> findAll(String search, RoomType roomType, RoomStatus status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND room_number LIKE ?");
            params.add("%" + search.trim() + "%");
        }
        if (roomType != null) {
            sql.append(" AND room_type = ?");
            params.add(roomType.getDisplayName());
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }

        sql.append(" ORDER BY room_number");

        return jdbcTemplate.query(sql.toString(), ROOM_ROW_MAPPER, params.toArray());
    }

    public Optional<Room> findById(Long id) {
        try {
            Room room = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE id = ?",
                    ROOM_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public boolean existsByRoomNumber(String roomNumber, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?"
                + (excludeId != null ? " AND id <> ?" : "");

        Integer count = excludeId != null
                ? jdbcTemplate.queryForObject(sql, Integer.class, roomNumber, excludeId)
                : jdbcTemplate.queryForObject(sql, Integer.class, roomNumber);

        return count != null && count > 0;
    }

    public Long save(Room room) {
        String sql = """
                INSERT INTO rooms (room_number, room_type, price_per_night, floor_number,
                                   max_occupancy, status, description)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().getDisplayName());
            ps.setBigDecimal(3, room.getPricePerNight());
            ps.setInt(4, room.getFloorNumber());
            ps.setInt(5, room.getMaxOccupancy());
            ps.setString(6, room.getStatus().name());
            ps.setString(7, room.getDescription());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(Room room) {
        String sql = """
                UPDATE rooms
                SET room_number = ?, room_type = ?, price_per_night = ?, floor_number = ?,
                    max_occupancy = ?, status = ?, description = ?
                WHERE id = ?
                """;

        return jdbcTemplate.update(
                sql,
                room.getRoomNumber(),
                room.getRoomType().getDisplayName(),
                room.getPricePerNight(),
                room.getFloorNumber(),
                room.getMaxOccupancy(),
                room.getStatus().name(),
                room.getDescription(),
                room.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM rooms WHERE id = ?", id);
    }

    public int updateStatus(Long id, RoomStatus status) {
        return jdbcTemplate.update("UPDATE rooms SET status = ? WHERE id = ?", status.name(), id);
    }
    public long countRooms() {
    Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rooms",
            Long.class
    );

    return count == null ? 0 : count;
}

public long countByStatus(RoomStatus status) {
    Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rooms WHERE status = ?",
            Long.class,
            status.name()
    );

    return count == null ? 0 : count;
}
}
