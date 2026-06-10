package com.shnoor;

import com.shnoor.dao.CustomerDAO;
import com.shnoor.dao.ProductDAO;
import com.shnoor.model.*;
import com.shnoor.service.ShipmentService;
import com.shnoor.util.TablePrinter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║   SHNOOR International LLC – Trade Management System        ║
 * ║   Technology Division Apprenticeship Project                 ║
 * ║   Stack: Java 17 · PostgreSQL · JDBC · OOP · SQL            ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Entry point. Provides an interactive console menu demonstrating
 * all CRUD operations, business logic, and aggregate reporting.
 *
 * Run: java -cp ".:lib/*" com.shnoor.Main
 */
public class Main {

    static final ShipmentService shipmentService = new ShipmentService();
    static final CustomerDAO     customerDAO     = new CustomerDAO();
    static final ProductDAO      productDAO      = new ProductDAO();
    static final Scanner         scanner         = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> listAllShipments();
                case "2"  -> createSampleExport();
                case "3"  -> advanceShipmentStatus();
                case "4"  -> listCustomers();
                case "5"  -> listProducts();
                case "6"  -> showLowStockAlerts();
                case "7"  -> revenueSummary();
                case "8"  -> filterByType();
                case "0"  -> { System.out.println("Goodbye!"); running = false; }
                default   -> System.out.println("[!] Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    // ------------------------------------------------------------------ MENU

    static void printBanner() {
        System.out.println("""
                \n
                ╔══════════════════════════════════════════════════════════╗
                ║   SHNOOR International LLC                               ║
                ║   Trade Management System  v1.0                          ║
                ║   Technology Division – Apprenticeship Project           ║
                ╚══════════════════════════════════════════════════════════╝
                """);
    }

    static void printMenu() {
        System.out.println("""
                ┌─────────────────────────────────────┐
                │  MAIN MENU                          │
                │  1. List all shipments              │
                │  2. Create sample export shipment   │
                │  3. Advance shipment status         │
                │  4. List customers                  │
                │  5. List products / inventory       │
                │  6. Low-stock alerts                │
                │  7. Revenue summary (aggregate)     │
                │  8. Filter shipments by type        │
                │  0. Exit                            │
                └─────────────────────────────────────┘
                Enter choice:\s""");
    }

    // ------------------------------------------------------------------ ACTIONS

    static void listAllShipments() {
        List<Shipment> list = shipmentService.getAllShipments();
        List<String> headers = List.of("ID", "Type", "Status", "Origin", "Dest", "Tracking", "Total ($)");
        List<List<String>> rows = new ArrayList<>();
        for (Shipment s : list) {
            rows.add(List.of(
                String.valueOf(s.getShipmentId()),
                s.getShipmentType().name(),
                s.getStatus().name(),
                s.getOriginCountry(),
                s.getDestCountry(),
                s.getTrackingNumber() != null ? s.getTrackingNumber() : "-",
                s.getTotalValue() != null ? s.getTotalValue().toPlainString() : "0"
            ));
        }
        System.out.println("\n── All Shipments ──");
        TablePrinter.print(headers, rows);
    }

    static void createSampleExport() {
        System.out.println("\n── Creating sample EXPORT shipment ──");

        // Build shipment with items
        Shipment s = new Shipment(
            ShipmentType.EXPORT,
            1,   // customer_id: GlobalTech Inc.
            null,
            "USA",
            "Germany",
            LocalDate.now(),
            LocalDate.now().plusDays(14),
            "SH-" + System.currentTimeMillis(),
            1    // created_by: admin
        );

        ShipmentItem item = new ShipmentItem(0, 1, 5, BigDecimal.ZERO); // productId=1, qty=5
        s.getItems().add(item);

        try {
            Shipment created = shipmentService.createShipment(s);
            System.out.println("[OK] Created: " + created);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    static void advanceShipmentStatus() {
        System.out.print("Enter Shipment ID to advance: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Shipment s = shipmentService.advanceStatus(id);
            System.out.println("[OK] New status: " + s.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID.");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    static void listCustomers() {
        List<Customer> list = customerDAO.findAll();
        List<String> headers = List.of("ID", "Name", "Country", "Email", "Phone");
        List<List<String>> rows = new ArrayList<>();
        for (Customer c : list) {
            rows.add(List.of(
                String.valueOf(c.getCustomerId()),
                c.getName(),
                c.getCountry(),
                c.getEmail(),
                c.getPhone() != null ? c.getPhone() : "-"
            ));
        }
        System.out.println("\n── Customers ──");
        TablePrinter.print(headers, rows);
    }

    static void listProducts() {
        List<Product> list = productDAO.findAll();
        List<String> headers = List.of("ID", "Name", "HS Code", "Price", "Unit", "Stock");
        List<List<String>> rows = new ArrayList<>();
        for (Product p : list) {
            rows.add(List.of(
                String.valueOf(p.getProductId()),
                p.getProductName(),
                p.getHsCode() != null ? p.getHsCode() : "-",
                "$" + p.getUnitPrice().toPlainString(),
                p.getUnit(),
                String.valueOf(p.getStockQty())
            ));
        }
        System.out.println("\n── Products / Inventory ──");
        TablePrinter.print(headers, rows);
    }

    static void showLowStockAlerts() {
        System.out.print("Enter stock threshold (e.g. 100): ");
        try {
            int threshold = Integer.parseInt(scanner.nextLine().trim());
            List<Product> list = shipmentService.getLowStockAlerts(threshold);
            if (list.isEmpty()) {
                System.out.println("[OK] No products below threshold " + threshold + ".");
                return;
            }
            List<String> headers = List.of("ID", "Product", "Stock", "Unit");
            List<List<String>> rows = new ArrayList<>();
            for (Product p : list) {
                rows.add(List.of(
                    String.valueOf(p.getProductId()),
                    p.getProductName(),
                    String.valueOf(p.getStockQty()),
                    p.getUnit()
                ));
            }
            System.out.println("\n── Low Stock Alert (< " + threshold + ") ──");
            TablePrinter.print(headers, rows);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid number.");
        }
    }

    static void revenueSummary() {
        shipmentService.printRevenueSummary();
    }

    static void filterByType() {
        System.out.print("Enter type (IMPORT / EXPORT): ");
        try {
            ShipmentType type = ShipmentType.valueOf(scanner.nextLine().trim().toUpperCase());
            List<Shipment> list = shipmentService.getShipmentsByType(type);
            List<String> headers = List.of("ID", "Status", "Origin", "Dest", "Shipping Date", "Total ($)");
            List<List<String>> rows = new ArrayList<>();
            for (Shipment s : list) {
                rows.add(List.of(
                    String.valueOf(s.getShipmentId()),
                    s.getStatus().name(),
                    s.getOriginCountry(),
                    s.getDestCountry(),
                    s.getShippingDate() != null ? s.getShippingDate().toString() : "-",
                    s.getTotalValue() != null ? s.getTotalValue().toPlainString() : "0"
                ));
            }
            System.out.println("\n── " + type + " Shipments ──");
            TablePrinter.print(headers, rows);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Invalid type. Use IMPORT or EXPORT.");
        }
    }
}
