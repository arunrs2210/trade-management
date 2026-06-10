package com.shnoor.dao;

import com.shnoor.config.DBConnection;
import com.shnoor.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for the customers table.
 * Demonstrates SQL CRUD operations with PreparedStatements.
 */
public class CustomerDAO implements GenericDAO<Customer, Integer> {

    // ------------------------------------------------------------------ INSERT
    @Override
    public Customer save(Customer c) {
        String sql = """
                INSERT INTO customers (name, email, phone, country, address)
                VALUES (?, ?, ?, ?, ?)
                RETURNING customer_id, created_at
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getCountry());
            ps.setString(5, c.getAddress());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c.setCustomerId(rs.getInt("customer_id"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.save failed: " + e.getMessage(), e);
        }
        return c;
    }

    // ------------------------------------------------------------------ SELECT by ID
    @Override
    public Optional<Customer> findById(Integer id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.findById failed", e);
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ SELECT ALL
    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY name";
        List<Customer> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.findAll failed", e);
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE
    @Override
    public boolean update(Customer c) {
        String sql = """
                UPDATE customers
                   SET name=?, email=?, phone=?, country=?, address=?
                 WHERE customer_id=?
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getCountry());
            ps.setString(5, c.getAddress());
            ps.setInt(6, c.getCustomerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.update failed", e);
        }
    }

    // ------------------------------------------------------------------ DELETE
    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.delete failed", e);
        }
    }

    // ------------------------------------------------------------------ SEARCH by country
    public List<Customer> findByCountry(String country) {
        String sql = "SELECT * FROM customers WHERE LOWER(country) = LOWER(?) ORDER BY name";
        List<Customer> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, country);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CustomerDAO.findByCountry failed", e);
        }
        return list;
    }

    // ------------------------------------------------------------------ Mapper
    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setCountry(rs.getString("country"));
        c.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
