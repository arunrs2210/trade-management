package com.shnoor.dao;

import com.shnoor.config.DBConnection;
import com.shnoor.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for the products table.
 */
public class ProductDAO implements GenericDAO<Product, Integer> {

    @Override
    public Product save(Product p) {
        String sql = """
                INSERT INTO products (product_name, hs_code, unit_price, unit, stock_qty, supplier_id)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING product_id, created_at
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getProductName());
            ps.setString(2, p.getHsCode());
            ps.setBigDecimal(3, p.getUnitPrice());
            ps.setString(4, p.getUnit());
            ps.setInt(5, p.getStockQty());
            ps.setInt(6, p.getSupplierId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setProductId(rs.getInt("product_id"));
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.save failed", e);
        }
        return p;
    }

    @Override
    public Optional<Product> findById(Integer id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.findById failed", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_name";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.findAll failed", e);
        }
        return list;
    }

    @Override
    public boolean update(Product p) {
        String sql = """
                UPDATE products
                   SET product_name=?, hs_code=?, unit_price=?, unit=?, stock_qty=?, supplier_id=?
                 WHERE product_id=?
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getProductName());
            ps.setString(2, p.getHsCode());
            ps.setBigDecimal(3, p.getUnitPrice());
            ps.setString(4, p.getUnit());
            ps.setInt(5, p.getStockQty());
            ps.setInt(6, p.getSupplierId());
            ps.setInt(7, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.update failed", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.delete failed", e);
        }
    }

    /** Adjust stock quantity by a delta (positive = add, negative = deduct). */
    public boolean adjustStock(int productId, int delta) {
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE product_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.adjustStock failed", e);
        }
    }

    /** Low-stock alert: products with stock below threshold. */
    public List<Product> findLowStock(int threshold) {
        String sql = "SELECT * FROM products WHERE stock_qty < ? ORDER BY stock_qty";
        List<Product> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ProductDAO.findLowStock failed", e);
        }
        return list;
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setHsCode(rs.getString("hs_code"));
        p.setUnitPrice(rs.getBigDecimal("unit_price"));
        p.setUnit(rs.getString("unit"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setSupplierId(rs.getInt("supplier_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}
