package com.hotel.model;

import java.io.Serializable;

public class DashboardData implements Serializable {

    private static final long serialVersionUID = 1L;

    private DashboardStats stats;
    private ChartSeries roomStatusChart;
    private ChartSeries bookingStatusChart;
    private ChartSeries revenueChart;

    public DashboardStats getStats() {
        return stats;
    }

    public void setStats(DashboardStats stats) {
        this.stats = stats;
    }

    public ChartSeries getRoomStatusChart() {
        return roomStatusChart;
    }

    public void setRoomStatusChart(ChartSeries roomStatusChart) {
        this.roomStatusChart = roomStatusChart;
    }

    public ChartSeries getBookingStatusChart() {
        return bookingStatusChart;
    }

    public void setBookingStatusChart(ChartSeries bookingStatusChart) {
        this.bookingStatusChart = bookingStatusChart;
    }

    public ChartSeries getRevenueChart() {
        return revenueChart;
    }

    public void setRevenueChart(ChartSeries revenueChart) {
        this.revenueChart = revenueChart;
    }
}
