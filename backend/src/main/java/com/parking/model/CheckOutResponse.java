package com.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckOutResponse(
        String ticketId,
        String vehicleNumber,
        int slotNumber,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        long parkedMinutes,
        BigDecimal charge
) {
}
