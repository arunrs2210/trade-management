package com.shnoor.dao;

import com.shnoor.config.DBConnection;
import com.shnoor.model.ShipmentItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipmentItemDAO implements GenericDAO<ShipmentItem, Integer> {

    /** Used internally by ShipmentDAO within an existing transaction. */
    public ShipmentItem save(ShipmentItem item, Connection con) throws SQLException {
        String sql = """
                INSERT INTO shipment_items (shipment_id, product_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                RETURNING item_id
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getShipmentId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) item.setItemId(rs.getInt("item_id"));
            }
        }
        return item;
    }

    @Override
    public ShipmentItem save(ShipmentItem item) {
        try (Connection con = DBConnection.getInstance().getConnection()) {
            return save(item, con);
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.save failed", e);
        }
    }

    @Override
    public Optional<ShipmentItem> findById(Integer id) {
        String sql = "SELECT * FROM shipment_items WHERE item_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.findById failed", e);
        }
        return Optional.empty();
    }

    public List<ShipmentItem> findByShipmentId(int shipmentId) {
        String sql = "SELECT * FROM shipment_items WHERE shipment_id = ?";
        List<ShipmentItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, shipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.findByShipmentId failed", e);
        }
        return list;
    }

    @Override
    public List<ShipmentItem> findAll() {
        List<ShipmentItem> list = new ArrayList<>();
        String sql = "SELECT * FROM shipment_items";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.findAll failed", e);
        }
        return list;
    }

    @Override
    public boolean update(ShipmentItem item) {
        String sql = "UPDATE shipment_items SET quantity=?, unit_price=? WHERE item_id=?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantity());
            ps.setBigDecimal(2, item.getUnitPrice());
            ps.setInt(3, item.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.update failed", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM shipment_items WHERE item_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentItemDAO.delete failed", e);
        }
    }

    private ShipmentItem map(ResultSet rs) throws SQLException {
        ShipmentItem i = new ShipmentItem();
        i.setItemId(rs.getInt("item_id"));
        i.setShipmentId(rs.getInt("shipment_id"));
        i.setProductId(rs.getInt("product_id"));
        i.setQuantity(rs.getInt("quantity"));
        i.setUnitPrice(rs.getBigDecimal("unit_price"));
        return i;
    }
}
