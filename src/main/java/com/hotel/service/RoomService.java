package com.hotel.service;

import com.hotel.dao.RoomDao;
import com.hotel.exception.HotelException;
import com.hotel.model.Room;
import com.hotel.model.enums.RoomStatus;
import com.hotel.model.enums.RoomType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomDao roomDao;

    public RoomService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    public List<Room> findRooms(String search, RoomType roomType, RoomStatus status) {
        return roomDao.findAll(search, roomType, status);
    }

    public Room getRoomById(Long id) {
        return roomDao.findById(id)
                .orElseThrow(() -> new HotelException("Room not found with id: " + id));
    }

    @Transactional
    public Room createRoom(Room room) {
        validateRoom(room, null);
        applyDefaults(room);
        Long id = roomDao.save(room);
        room.setId(id);
        return room;
    }

    @Transactional
    public Room updateRoom(Long id, Room room) {
        Room existing = getRoomById(id);
        room.setId(id);

        if (room.getFloorNumber() == null) {
            room.setFloorNumber(existing.getFloorNumber());
        }
        if (room.getMaxOccupancy() == null) {
            room.setMaxOccupancy(existing.getMaxOccupancy());
        }
        if (room.getDescription() == null) {
            room.setDescription(existing.getDescription());
        }

        validateRoom(room, id);
        applyDefaults(room);
        roomDao.update(room);
        return room;
    }

    @Transactional
    public void deleteRoom(Long id) {
        getRoomById(id);
        try {
            int deleted = roomDao.deleteById(id);
            if (deleted == 0) {
                throw new HotelException("Room not found with id: " + id);
            }
        } catch (DataIntegrityViolationException ex) {
            throw new HotelException("Cannot delete room. It may have active bookings.");
        }
    }

    private void validateRoom(Room room, Long excludeId) {
        if (roomDao.existsByRoomNumber(room.getRoomNumber(), excludeId)) {
            throw new HotelException("Room number already exists: " + room.getRoomNumber());
        }
    }

    private void applyDefaults(Room room) {
        if (room.getFloorNumber() == null) {
            room.setFloorNumber(1);
        }
        if (room.getMaxOccupancy() == null) {
            room.setMaxOccupancy(resolveMaxOccupancy(room.getRoomType()));
        }
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
    }

    private int resolveMaxOccupancy(RoomType roomType) {
        if (roomType == RoomType.LUXURY_SINGLE || roomType == RoomType.DELUXE_SINGLE) {
            return 1;
        }
        return 2;
    }
}
