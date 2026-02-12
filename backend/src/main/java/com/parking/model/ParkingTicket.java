package com.parking.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class ParkingTicket {
    private final String ticketId;
    private final String vehicleNumber;
    private final int slotNumber;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal charge;

    public ParkingTicket(String ticketId, String vehicleNumber, int slotNumber, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void closeTicket(LocalDateTime exitTime, BigDecimal charge) {
        this.exitTime = exitTime;
        this.charge = charge;
    }

    public long parkedMinutes() {
        LocalDateTime end = exitTime != null ? exitTime : LocalDateTime.now();
        return Math.max(1, Duration.between(entryTime, end).toMinutes());
    }
}
