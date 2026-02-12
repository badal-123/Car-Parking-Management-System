package com.parking.service;

import com.parking.model.CheckInResponse;
import com.parking.model.CheckOutResponse;
import com.parking.model.DashboardResponse;

public class ParkingServiceTest {

    public static void main(String[] args) {
        shouldAllocateSlotAndUpdateDashboard();
        shouldReleaseSlotAndReturnCharge();
        shouldRejectDuplicateVehicle();
        System.out.println("All backend service tests passed.");
    }

    private static void shouldAllocateSlotAndUpdateDashboard() {
        ParkingService service = new ParkingService();
        CheckInResponse response = service.checkIn("KA01AB1234");

        assertTrue(response.ticketId() != null && !response.ticketId().isBlank(), "ticket id should be generated");
        assertTrue(response.slotNumber() == 1, "first slot should be allocated");

        DashboardResponse dashboard = service.dashboard();
        assertTrue(dashboard.occupiedSlots() == 1, "occupied slots should be 1");
        assertTrue(dashboard.availableSlots() == 11, "available slots should be 11");
    }

    private static void shouldReleaseSlotAndReturnCharge() {
        ParkingService service = new ParkingService();
        CheckInResponse entry = service.checkIn("KA05CD6789");

        CheckOutResponse exit = service.checkOut(entry.ticketId());

        assertTrue(entry.ticketId().equals(exit.ticketId()), "checkout should refer same ticket");
        assertTrue(exit.charge() != null, "charge should be returned");
        assertTrue(exit.charge().doubleValue() > 0, "charge should be positive");

        DashboardResponse dashboard = service.dashboard();
        assertTrue(dashboard.occupiedSlots() == 0, "slot should be released");
    }

    private static void shouldRejectDuplicateVehicle() {
        ParkingService service = new ParkingService();
        service.checkIn("MH12XY0001");
        boolean rejected = false;

        try {
            service.checkIn("MH12XY0001");
        } catch (ApiException ex) {
            rejected = ex.getStatusCode() == 409 && "Vehicle already checked in".equals(ex.getMessage());
        }

        assertTrue(rejected, "duplicate vehicle must be rejected with 409");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Assertion failed: " + message);
        }
    }
}
