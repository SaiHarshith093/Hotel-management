package com.hotel.service;

import com.hotel.dao.DashboardDao;
import com.hotel.model.DashboardData;
import com.hotel.model.DashboardStats;
import com.hotel.model.enums.RoomStatus;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final DashboardDao dashboardDao;

    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public DashboardData getDashboardData() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalRooms(dashboardDao.countTotalRooms());
        stats.setAvailableRooms(dashboardDao.countRoomsByStatus(RoomStatus.AVAILABLE));
        stats.setOccupiedRooms(dashboardDao.countRoomsByStatus(RoomStatus.OCCUPIED));
        stats.setTotalCustomers(dashboardDao.countTotalCustomers());
        stats.setActiveBookings(dashboardDao.countActiveBookings());
        stats.setRevenueToday(dashboardDao.sumRevenueToday());

        DashboardData data = new DashboardData();
        data.setStats(stats);
        data.setRoomStatusChart(dashboardDao.findRoomStatusChart());
        data.setBookingStatusChart(dashboardDao.findBookingStatusChart());
        data.setRevenueChart(dashboardDao.findRevenueLast7DaysChart());
        return data;
    }
}
