package com.shnoor.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an import or export shipment in the SHNOOR trade system.
 * Demonstrates OOP: encapsulation, composition (ShipmentItem list), and enums.
 */
public class Shipment {

    private int            shipmentId;
    private ShipmentType   shipmentType;
    private ShipmentStatus status;
    private Integer        customerId;
    private Integer        supplierId;
    private String         originCountry;
    private String         destCountry;
    private LocalDate      shippingDate;
    private LocalDate      expectedArrival;
    private LocalDate      actualArrival;
    private String         trackingNumber;
    private BigDecimal     totalValue;
    private int            createdBy;
    private LocalDateTime  createdAt;

    // Composed list of line items
    private List<ShipmentItem> items = new ArrayList<>();

    public Shipment() {}

    public Shipment(ShipmentType type, Integer customerId, Integer supplierId,
                    String origin, String dest, LocalDate shipping, LocalDate expected,
                    String tracking, int createdBy) {
        this.shipmentType    = type;
        this.status          = ShipmentStatus.PENDING;
        this.customerId      = customerId;
        this.supplierId      = supplierId;
        this.originCountry   = origin;
        this.destCountry     = dest;
        this.shippingDate    = shipping;
        this.expectedArrival = expected;
        this.trackingNumber  = tracking;
        this.createdBy       = createdBy;
        this.totalValue      = BigDecimal.ZERO;
    }

    /** Recalculates totalValue from items. */
    public void recalculateTotal() {
        totalValue = items.stream()
                .map(ShipmentItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---------- Getters & Setters ----------

    public int getShipmentId()                   { return shipmentId; }
    public void setShipmentId(int id)            { this.shipmentId = id; }

    public ShipmentType getShipmentType()        { return shipmentType; }
    public void setShipmentType(ShipmentType t)  { this.shipmentType = t; }

    public ShipmentStatus getStatus()            { return status; }
    public void setStatus(ShipmentStatus s)      { this.status = s; }

    public Integer getCustomerId()               { return customerId; }
    public void setCustomerId(Integer id)        { this.customerId = id; }

    public Integer getSupplierId()               { return supplierId; }
    public void setSupplierId(Integer id)        { this.supplierId = id; }

    public String getOriginCountry()             { return originCountry; }
    public void setOriginCountry(String c)       { this.originCountry = c; }

    public String getDestCountry()               { return destCountry; }
    public void setDestCountry(String c)         { this.destCountry = c; }

    public LocalDate getShippingDate()           { return shippingDate; }
    public void setShippingDate(LocalDate d)     { this.shippingDate = d; }

    public LocalDate getExpectedArrival()        { return expectedArrival; }
    public void setExpectedArrival(LocalDate d)  { this.expectedArrival = d; }

    public LocalDate getActualArrival()          { return actualArrival; }
    public void setActualArrival(LocalDate d)    { this.actualArrival = d; }

    public String getTrackingNumber()            { return trackingNumber; }
    public void setTrackingNumber(String t)      { this.trackingNumber = t; }

    public BigDecimal getTotalValue()            { return totalValue; }
    public void setTotalValue(BigDecimal v)      { this.totalValue = v; }

    public int getCreatedBy()                    { return createdBy; }
    public void setCreatedBy(int u)              { this.createdBy = u; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)   { this.createdAt = dt; }

    public List<ShipmentItem> getItems()         { return items; }
    public void setItems(List<ShipmentItem> i)   { this.items = i; }

    @Override
    public String toString() {
        return String.format(
            "Shipment{id=%d, type=%s, status=%s, tracking='%s', %s→%s, total=$%s}",
            shipmentId, shipmentType, status, trackingNumber,
            originCountry, destCountry, totalValue);
    }
}
