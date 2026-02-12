package com.parking;

import com.parking.model.CheckInResponse;
import com.parking.model.CheckOutResponse;
import com.parking.model.DashboardResponse;
import com.parking.model.ParkingSlotView;
import com.parking.service.ApiException;
import com.parking.service.ParkingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParkingManagementApplication {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void main(String[] args) throws IOException {
        ParkingService parkingService = new ParkingService();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/parking/health", exchange -> {
            if (isOptions(exchange)) {
                sendOptions(exchange);
                return;
            }
            try {
                requireMethod(exchange, "GET");
                sendJson(exchange, 200, "{\"status\":\"UP\"}");
            } catch (ApiException e) {
                sendError(exchange, e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Internal server error");
            }
        });

        server.createContext("/api/parking/dashboard", exchange -> {
            if (isOptions(exchange)) {
                sendOptions(exchange);
                return;
            }
            try {
                requireMethod(exchange, "GET");
                DashboardResponse response = parkingService.dashboard();
                sendJson(exchange, 200, toJson(response));
            } catch (ApiException e) {
                sendError(exchange, e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Internal server error");
            }
        });

        server.createContext("/api/parking/check-in", exchange -> handleParkingMutation(exchange, parkingService, "vehicleNumber", true));
        server.createContext("/api/parking/check-out", exchange -> handleParkingMutation(exchange, parkingService, "ticketId", false));

        server.setExecutor(null);
        server.start();
        System.out.println("Parking backend running on http://localhost:8080");
    }

    private static void handleParkingMutation(HttpExchange exchange, ParkingService parkingService, String fieldName, boolean isCheckIn) throws IOException {
        if (isOptions(exchange)) {
            sendOptions(exchange);
            return;
        }

        try {
            requireMethod(exchange, "POST");
            String body = readBody(exchange);
            String value = extractJsonField(body, fieldName);
            if (isCheckIn) {
                CheckInResponse response = parkingService.checkIn(value);
                sendJson(exchange, 200, toJson(response));
            } else {
                CheckOutResponse response = parkingService.checkOut(value);
                sendJson(exchange, 200, toJson(response));
            }
        } catch (ApiException e) {
            sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error");
        }
    }

    private static boolean isOptions(HttpExchange exchange) {
        return "OPTIONS".equalsIgnoreCase(exchange.getRequestMethod());
    }

    private static void requireMethod(HttpExchange exchange, String method) {
        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
            throw new ApiException(405, "Method not allowed");
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extractJsonField(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static void sendOptions(HttpExchange exchange) throws IOException {
        addCors(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, "{\"message\":\"" + escape(message) + "\"}");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCors(exchange);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String toJson(CheckInResponse response) {
        return "{" +
                "\"ticketId\":\"" + escape(response.ticketId()) + "\"," +
                "\"vehicleNumber\":\"" + escape(response.vehicleNumber()) + "\"," +
                "\"slotNumber\":" + response.slotNumber() + "," +
                "\"entryTime\":\"" + response.entryTime().format(TIME_FORMAT) + "\"" +
                "}";
    }

    private static String toJson(CheckOutResponse response) {
        return "{" +
                "\"ticketId\":\"" + escape(response.ticketId()) + "\"," +
                "\"vehicleNumber\":\"" + escape(response.vehicleNumber()) + "\"," +
                "\"slotNumber\":" + response.slotNumber() + "," +
                "\"entryTime\":\"" + response.entryTime().format(TIME_FORMAT) + "\"," +
                "\"exitTime\":\"" + response.exitTime().format(TIME_FORMAT) + "\"," +
                "\"parkedMinutes\":" + response.parkedMinutes() + "," +
                "\"charge\":" + response.charge() +
                "}";
    }

    private static String toJson(DashboardResponse response) {
        List<ParkingSlotView> slots = response.slots();
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"totalSlots\":").append(response.totalSlots()).append(',')
                .append("\"occupiedSlots\":").append(response.occupiedSlots()).append(',')
                .append("\"availableSlots\":").append(response.availableSlots()).append(',')
                .append("\"slots\":[");

        for (int i = 0; i < slots.size(); i++) {
            ParkingSlotView slot = slots.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{")
                    .append("\"slotNumber\":").append(slot.slotNumber()).append(',')
                    .append("\"section\":\"").append(escape(slot.section())).append("\",")
                    .append("\"occupied\":").append(slot.occupied()).append(',')
                    .append("\"vehicleNumber\":");
            if (slot.vehicleNumber() == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escape(slot.vehicleNumber())).append("\"");
            }
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
