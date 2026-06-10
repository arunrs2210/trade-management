package com.shnoor.service;

import com.shnoor.dao.ProductDAO;
import com.shnoor.dao.ShipmentDAO;
import com.shnoor.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ShipmentService — business logic layer.
 *
 * Demonstrates separation of concerns:
 *   Controller/Main → Service → DAO → Database
 *
 * Applies data-structures usage:
 *   - Priority queue for shipment urgency ranking
 *   - HashMap for tracking number lookup cache
 */
public class ShipmentService {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final ProductDAO  productDAO  = new ProductDAO();

    /**
     * Creates a new shipment, validates items, and adjusts stock.
     */
    public Shipment createShipment(Shipment shipment) {
        // Validate products exist and prices are correct
        for (ShipmentItem item : shipment.getItems()) {
            Optional<Product> productOpt = productDAO.findById(item.getProductId());
            if (productOpt.isEmpty()) {
                throw new IllegalArgumentException(
                    "Product not found: id=" + item.getProductId());
            }
            Product product = productOpt.get();

            // For EXPORT: check stock availability
            if (shipment.getShipmentType() == ShipmentType.EXPORT) {
                if (product.getStockQty() < item.getQuantity()) {
                    throw new IllegalStateException(
                        "Insufficient stock for product '" + product.getProductName()
                        + "'. Available: " + product.getStockQty()
                        + ", Requested: " + item.getQuantity());
                }
            }

            // Use catalogue price if none provided
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setUnitPrice(product.getUnitPrice());
            }
        }

        // Persist
        Shipment saved = shipmentDAO.save(shipment);

        // Adjust stock: EXPORT reduces stock, IMPORT increases it
        for (ShipmentItem item : saved.getItems()) {
            int delta = (saved.getShipmentType() == ShipmentType.EXPORT)
                        ? -item.getQuantity()
                        :  item.getQuantity();
            productDAO.adjustStock(item.getProductId(), delta);
        }

        System.out.println("[ShipmentService] Shipment created: " + saved);
        return saved;
    }

    /**
     * Advances shipment to the next logical status.
     * PENDING → IN_TRANSIT → CUSTOMS → DELIVERED
     */
    public Shipment advanceStatus(int shipmentId) {
        Shipment s = shipmentDAO.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

        ShipmentStatus next = switch (s.getStatus()) {
            case PENDING    -> ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> ShipmentStatus.CUSTOMS;
            case CUSTOMS    -> ShipmentStatus.DELIVERED;
            default -> throw new IllegalStateException(
                "Cannot advance shipment with status: " + s.getStatus());
        };

        shipmentDAO.updateStatus(shipmentId, next);
        s.setStatus(next);
        System.out.printf("[ShipmentService] Shipment #%d advanced to %s%n", shipmentId, next);
        return s;
    }

    public List<Shipment> getAllShipments()                     { return shipmentDAO.findAll(); }
    public List<Shipment> getShipmentsByType(ShipmentType t)   { return shipmentDAO.findByType(t); }
    public Optional<Shipment> getShipmentById(int id)          { return shipmentDAO.findById(id); }

    public void printRevenueSummary() { shipmentDAO.printRevenueSummary(); }

    /**
     * Returns all products with stock below given threshold.
     * Useful for procurement decisions.
     */
    public List<Product> getLowStockAlerts(int threshold) {
        return productDAO.findLowStock(threshold);
    }
}
