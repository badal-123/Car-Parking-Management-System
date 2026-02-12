package com.parking.model;

import java.time.LocalDateTime;

public record CheckInResponse(
        String ticketId,
        String vehicleNumber,
        int slotNumber,
        LocalDateTime entryTime
) {
}
