package com.shnoor.model;

import java.time.LocalDateTime;

/**
 * Represents a customer in the SHNOOR trade system.
 */
public class Customer {

    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String address;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(String name, String email, String phone, String country, String address) {
        this.name    = name;
        this.email   = email;
        this.phone   = phone;
        this.country = country;
        this.address = address;
    }

    // ---------- Getters & Setters ----------

    public int getCustomerId()            { return customerId; }
    public void setCustomerId(int id)     { this.customerId = id; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getPhone()              { return phone; }
    public void setPhone(String phone)    { this.phone = phone; }

    public String getCountry()            { return country; }
    public void setCountry(String c)      { this.country = c; }

    public String getAddress()            { return address; }
    public void setAddress(String a)      { this.address = a; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)  { this.createdAt = dt; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', country='%s', email='%s'}",
                customerId, name, country, email);
    }
}
