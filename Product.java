package com.shnoor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a tradeable product in the SHNOOR catalogue.
 */
public class Product {

    private int        productId;
    private String     productName;
    private String     hsCode;          // Harmonized System trade code
    private BigDecimal unitPrice;
    private String     unit;
    private int        stockQty;
    private int        supplierId;
    private LocalDateTime createdAt;

    public Product() {}

    public Product(String productName, String hsCode, BigDecimal unitPrice,
                   String unit, int stockQty, int supplierId) {
        this.productName = productName;
        this.hsCode      = hsCode;
        this.unitPrice   = unitPrice;
        this.unit        = unit;
        this.stockQty    = stockQty;
        this.supplierId  = supplierId;
    }

    // ---------- Getters & Setters ----------

    public int getProductId()               { return productId; }
    public void setProductId(int id)        { this.productId = id; }

    public String getProductName()          { return productName; }
    public void setProductName(String n)    { this.productName = n; }

    public String getHsCode()               { return hsCode; }
    public void setHsCode(String hs)        { this.hsCode = hs; }

    public BigDecimal getUnitPrice()        { return unitPrice; }
    public void setUnitPrice(BigDecimal p)  { this.unitPrice = p; }

    public String getUnit()                 { return unit; }
    public void setUnit(String u)           { this.unit = u; }

    public int getStockQty()                { return stockQty; }
    public void setStockQty(int qty)        { this.stockQty = qty; }

    public int getSupplierId()              { return supplierId; }
    public void setSupplierId(int id)       { this.supplierId = id; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)   { this.createdAt = dt; }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%s, stock=%d %s}",
                productId, productName, unitPrice, stockQty, unit);
    }
}
