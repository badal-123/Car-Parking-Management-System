package com.parking.service;

import com.parking.model.CheckInResponse;
import com.parking.model.CheckOutResponse;
import com.parking.model.DashboardResponse;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingSlotView;
import com.parking.model.ParkingTicket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class ParkingService {

    private static final BigDecimal RATE_PER_HOUR = BigDecimal.valueOf(30);

    private final Map<Integer, ParkingSlot> slots = new ConcurrentHashMap<>();
    private final Map<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();

    public ParkingService() {
        IntStream.rangeClosed(1, 12).forEach(i -> slots.put(i, new ParkingSlot(i, i <= 6 ? "A" : "B")));
    }

    public synchronized CheckInResponse checkIn(String vehicleNumber) {
        String normalizedVehicleNumber = normalize(vehicleNumber);
        if (normalizedVehicleNumber == null) {
            throw new ApiException(400, "Vehicle number is required");
        }

        if (activeTickets.values().stream().anyMatch(ticket -> ticket.getVehicleNumber().equalsIgnoreCase(normalizedVehicleNumber))) {
            throw new ApiException(409, "Vehicle already checked in");
        }

        ParkingSlot available = slots.values().stream()
                .filter(slot -> !slot.isOccupied())
                .min(Comparator.comparingInt(ParkingSlot::getSlotNumber))
                .orElseThrow(() -> new ApiException(409, "No slots available"));

        String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime entryTime = LocalDateTime.now();
        available.parkVehicle(normalizedVehicleNumber);

        ParkingTicket ticket = new ParkingTicket(ticketId, normalizedVehicleNumber, available.getSlotNumber(), entryTime);
        activeTickets.put(ticketId, ticket);

        return new CheckInResponse(ticketId, ticket.getVehicleNumber(), available.getSlotNumber(), entryTime);
    }

    public synchronized CheckOutResponse checkOut(String ticketId) {
        String normalizedTicketId = normalize(ticketId);
        if (normalizedTicketId == null) {
            throw new ApiException(400, "Ticket id is required");
        }

        ParkingTicket ticket = activeTickets.remove(normalizedTicketId);
        if (ticket == null) {
            throw new ApiException(404, "Ticket not found");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal hours = BigDecimal.valueOf(ticket.parkedMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.UP);
        BigDecimal charge = hours.multiply(RATE_PER_HOUR).setScale(2, RoundingMode.UP);
        ticket.closeTicket(exitTime, charge);

        ParkingSlot slot = slots.get(ticket.getSlotNumber());
        if (slot != null) {
            slot.clearSlot();
        }

        return new CheckOutResponse(
                ticket.getTicketId(),
                ticket.getVehicleNumber(),
                ticket.getSlotNumber(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                ticket.parkedMinutes(),
                ticket.getCharge()
        );
    }

    public DashboardResponse dashboard() {
        List<ParkingSlotView> slotViews = slots.values().stream()
                .sorted(Comparator.comparingInt(ParkingSlot::getSlotNumber))
                .map(slot -> new ParkingSlotView(slot.getSlotNumber(), slot.getSection(), slot.isOccupied(), slot.getVehicleNumber()))
                .toList();

        int occupied = (int) slotViews.stream().filter(ParkingSlotView::occupied).count();
        return new DashboardResponse(slotViews.size(), occupied, slotViews.size() - occupied, slotViews);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }
}
