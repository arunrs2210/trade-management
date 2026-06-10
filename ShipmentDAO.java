package com.shnoor.dao;

import com.shnoor.config.DBConnection;
import com.shnoor.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC DAO for the shipments + shipment_items tables.
 * Demonstrates transactions, JOINs, and aggregate queries.
 */
public class ShipmentDAO implements GenericDAO<Shipment, Integer> {

    private final ShipmentItemDAO itemDAO = new ShipmentItemDAO();

    // ------------------------------------------------------------------ INSERT (with transaction)
    @Override
    public Shipment save(Shipment s) {
        String sql = """
                INSERT INTO shipments
                  (shipment_type, status, customer_id, supplier_id,
                   origin_country, dest_country, shipping_date, expected_arrival,
                   tracking_number, total_value, created_by)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                RETURNING shipment_id, created_at
                """;
        try (Connection con = DBConnection.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, s.getShipmentType().name());
                ps.setString(2, s.getStatus().name());
                setNullableInt(ps, 3, s.getCustomerId());
                setNullableInt(ps, 4, s.getSupplierId());
                ps.setString(5, s.getOriginCountry());
                ps.setString(6, s.getDestCountry());
                ps.setObject(7, s.getShippingDate());
                ps.setObject(8, s.getExpectedArrival());
                ps.setString(9, s.getTrackingNumber());
                ps.setBigDecimal(10, s.getTotalValue());
                ps.setInt(11, s.getCreatedBy());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.setShipmentId(rs.getInt("shipment_id"));
                        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                }

                // Insert line items
                for (ShipmentItem item : s.getItems()) {
                    item.setShipmentId(s.getShipmentId());
                    itemDAO.save(item, con);
                }

                // Recalculate & persist total
                s.recalculateTotal();
                updateTotal(s.getShipmentId(), s.getTotalValue(), con);

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.save failed", e);
        }
        return s;
    }

    // ------------------------------------------------------------------ SELECT by ID
    @Override
    public Optional<Shipment> findById(Integer id) {
        String sql = "SELECT * FROM shipments WHERE shipment_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shipment s = map(rs);
                    s.setItems(itemDAO.findByShipmentId(id));
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.findById failed", e);
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ SELECT ALL
    @Override
    public List<Shipment> findAll() {
        String sql = "SELECT * FROM shipments ORDER BY created_at DESC";
        List<Shipment> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.findAll failed", e);
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE status only
    public boolean updateStatus(int shipmentId, ShipmentStatus newStatus) {
        String sql = "UPDATE shipments SET status = ? WHERE shipment_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, shipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.updateStatus failed", e);
        }
    }

    @Override
    public boolean update(Shipment s) {
        String sql = """
                UPDATE shipments
                   SET shipment_type=?, status=?, origin_country=?, dest_country=?,
                       shipping_date=?, expected_arrival=?, actual_arrival=?,
                       tracking_number=?, total_value=?
                 WHERE shipment_id=?
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getShipmentType().name());
            ps.setString(2, s.getStatus().name());
            ps.setString(3, s.getOriginCountry());
            ps.setString(4, s.getDestCountry());
            ps.setObject(5, s.getShippingDate());
            ps.setObject(6, s.getExpectedArrival());
            ps.setObject(7, s.getActualArrival());
            ps.setString(8, s.getTrackingNumber());
            ps.setBigDecimal(9, s.getTotalValue());
            ps.setInt(10, s.getShipmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.update failed", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM shipments WHERE shipment_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.delete failed", e);
        }
    }

    // ------------------------------------------------------------------ Filter by type
    public List<Shipment> findByType(ShipmentType type) {
        String sql = "SELECT * FROM shipments WHERE shipment_type = ? ORDER BY shipping_date DESC";
        List<Shipment> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.findByType failed", e);
        }
        return list;
    }

    // ------------------------------------------------------------------ Aggregate: total value by type
    public void printRevenueSummary() {
        String sql = """
                SELECT shipment_type,
                       COUNT(*)        AS shipment_count,
                       SUM(total_value) AS total_revenue
                  FROM shipments
                 WHERE status != 'CANCELLED'
                 GROUP BY shipment_type
                """;
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== Revenue Summary ==========");
            while (rs.next()) {
                System.out.printf("  %-8s | Shipments: %3d | Revenue: $%,.2f%n",
                        rs.getString("shipment_type"),
                        rs.getInt("shipment_count"),
                        rs.getBigDecimal("total_revenue"));
            }
            System.out.println("=====================================\n");
        } catch (SQLException e) {
            throw new RuntimeException("ShipmentDAO.printRevenueSummary failed", e);
        }
    }

    // ------------------------------------------------------------------ Helpers
    private void updateTotal(int id, java.math.BigDecimal total, Connection con) throws SQLException {
        String sql = "UPDATE shipments SET total_value = ? WHERE shipment_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }

    private Shipment map(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setShipmentId(rs.getInt("shipment_id"));
        s.setShipmentType(ShipmentType.valueOf(rs.getString("shipment_type")));
        s.setStatus(ShipmentStatus.valueOf(rs.getString("status")));
        int cid = rs.getInt("customer_id");
        s.setCustomerId(rs.wasNull() ? null : cid);
        int sid = rs.getInt("supplier_id");
        s.setSupplierId(rs.wasNull() ? null : sid);
        s.setOriginCountry(rs.getString("origin_country"));
        s.setDestCountry(rs.getString("dest_country"));
        Date sd = rs.getDate("shipping_date");
        if (sd != null) s.setShippingDate(sd.toLocalDate());
        Date ea = rs.getDate("expected_arrival");
        if (ea != null) s.setExpectedArrival(ea.toLocalDate());
        Date aa = rs.getDate("actual_arrival");
        if (aa != null) s.setActualArrival(aa.toLocalDate());
        s.setTrackingNumber(rs.getString("tracking_number"));
        s.setTotalValue(rs.getBigDecimal("total_value"));
        s.setCreatedBy(rs.getInt("created_by"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}
