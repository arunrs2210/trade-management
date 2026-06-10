package com.shnoor.model;

import java.math.BigDecimal;

/**
 * A single line item within a Shipment.
 */
public class ShipmentItem {

    private int        itemId;
    private int        shipmentId;
    private int        productId;
    private int        quantity;
    private BigDecimal unitPrice;

    public ShipmentItem() {}

    public ShipmentItem(int shipmentId, int productId, int quantity, BigDecimal unitPrice) {
        this.shipmentId = shipmentId;
        this.productId  = productId;
        this.quantity   = quantity;
        this.unitPrice  = unitPrice;
    }

    /** Computed line total (mirrors the SQL GENERATED column). */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ---------- Getters & Setters ----------

    public int getItemId()               { return itemId; }
    public void setItemId(int id)        { this.itemId = id; }

    public int getShipmentId()           { return shipmentId; }
    public void setShipmentId(int id)    { this.shipmentId = id; }

    public int getProductId()            { return productId; }
    public void setProductId(int id)     { this.productId = id; }

    public int getQuantity()             { return quantity; }
    public void setQuantity(int q)       { this.quantity = q; }

    public BigDecimal getUnitPrice()     { return unitPrice; }
    public void setUnitPrice(BigDecimal p) { this.unitPrice = p; }

    @Override
    public String toString() {
        return String.format("ShipmentItem{productId=%d, qty=%d, unit=$%s, total=$%s}",
                productId, quantity, unitPrice, getLineTotal());
    }
}
