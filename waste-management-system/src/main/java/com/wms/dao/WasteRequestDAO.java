package com.wms.dao;

import com.wms.db.DBConnection;
import com.wms.model.WasteRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WasteRequestDAO {

    private static final String BASE_SELECT =
        "SELECT r.*, u.full_name AS citizen_name, s.full_name AS staff_name " +
        "FROM waste_requests r " +
        "JOIN users u ON r.user_id = u.user_id " +
        "LEFT JOIN users s ON r.assigned_staff_id = s.user_id ";

    public void create(WasteRequest r) throws SQLException {
        String sql = "INSERT INTO waste_requests (user_id, waste_type, quantity, address, pickup_date, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getWasteType());
            ps.setString(3, r.getQuantity());
            ps.setString(4, r.getAddress());
            ps.setDate(5, r.getPickupDate());
            ps.setString(6, r.getRemarks());
            ps.executeUpdate();
        }
    }

    public void updateStatusAndAssignment(int requestId, String status, Integer staffId, String remarks) throws SQLException {
        String sql = "UPDATE waste_requests SET status = ?, assigned_staff_id = ?, remarks = ? WHERE request_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            if (staffId == null) ps.setNull(2, Types.NUMERIC); else ps.setInt(2, staffId);
            ps.setString(3, remarks);
            ps.setInt(4, requestId);
            ps.executeUpdate();
        }
    }

    public void delete(int requestId) throws SQLException {
        String sql = "DELETE FROM waste_requests WHERE request_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public List<WasteRequest> getAll() throws SQLException {
        List<WasteRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY r.request_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<WasteRequest> getByUser(int userId) throws SQLException {
        List<WasteRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.user_id = ? ORDER BY r.request_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<WasteRequest> getByAssignedStaff(int staffId) throws SQLException {
        List<WasteRequest> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.assigned_staff_id = ? ORDER BY r.request_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns counts of requests grouped by status, e.g. {PENDING=4, COLLECTED=10, ...} */
    public Map<String, Integer> getCountsByStatus() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM waste_requests GROUP BY status";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString("status"), rs.getInt("cnt"));
        }
        return counts;
    }

    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM waste_requests";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private WasteRequest mapRow(ResultSet rs) throws SQLException {
        WasteRequest r = new WasteRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setCitizenName(rs.getString("citizen_name"));
        r.setWasteType(rs.getString("waste_type"));
        r.setQuantity(rs.getString("quantity"));
        r.setAddress(rs.getString("address"));
        r.setPickupDate(rs.getDate("pickup_date"));
        r.setStatus(rs.getString("status"));
        int staffId = rs.getInt("assigned_staff_id");
        r.setAssignedStaffId(rs.wasNull() ? null : staffId);
        r.setAssignedStaffName(rs.getString("staff_name"));
        r.setRemarks(rs.getString("remarks"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
