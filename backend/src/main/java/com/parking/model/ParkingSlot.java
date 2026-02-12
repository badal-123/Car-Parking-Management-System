package com.parking.model;

public class ParkingSlot {
    private final int slotNumber;
    private final String section;
    private boolean occupied;
    private String vehicleNumber;

    public ParkingSlot(int slotNumber, String section) {
        this.slotNumber = slotNumber;
        this.section = section;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getSection() {
        return section;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void parkVehicle(String vehicleNumber) {
        this.occupied = true;
        this.vehicleNumber = vehicleNumber;
    }

    public void clearSlot() {
        this.occupied = false;
        this.vehicleNumber = null;
    }
}
