package com.parking.model;

import java.util.List;

public record DashboardResponse(
        int totalSlots,
        int occupiedSlots,
        int availableSlots,
        List<ParkingSlotView> slots
) {
}
