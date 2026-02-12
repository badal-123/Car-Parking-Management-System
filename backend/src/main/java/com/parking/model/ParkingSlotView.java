package com.parking.model;

public record ParkingSlotView(
        int slotNumber,
        String section,
        boolean occupied,
        String vehicleNumber
) {
}
