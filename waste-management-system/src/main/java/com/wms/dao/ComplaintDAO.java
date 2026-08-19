package com.wms.dao;

import com.wms.db.DBConnection;
import com.wms.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComplaintDAO
{

    private static final String BASE_SELECT =
        "SELECT c.*, u.full_name AS citizen_name FROM complaints c JOIN users u ON c.user_id = u.user_id ";

    public void create(Complaint c) throws SQLException
    {
        String sql = "INSERT INTO complaints (user_id, subject, description) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, c.getUserId());
            ps.setString(2, c.getSubject());
            ps.setString(3, c.getDescription());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int complaintId, String status) throws SQLException
    {
        String sql = "UPDATE complaints SET status = ?, resolved_at = ? WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, status);
            if ("RESOLVED".equals(status))
            {
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            } else
            {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, complaintId);
            ps.executeUpdate();
        }
    }

    public void delete(int complaintId) throws SQLException
    {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        }
    }

    public List<Complaint> getAll() throws SQLException
    {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.complaint_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Complaint> getByUser(int userId) throws SQLException
    {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.user_id = ? ORDER BY c.complaint_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Map<String, Integer> getCountsByStatus() throws SQLException
    {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM complaints GROUP BY status";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) counts.put(rs.getString("status"), rs.getInt("cnt"));
        }
        return counts;
    }

    public int getTotalCount() throws SQLException
    {
        String sql = "SELECT COUNT(*) FROM complaints";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Complaint mapRow(ResultSet rs) throws SQLException
    {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setCitizenName(rs.getString("citizen_name"));
        c.setSubject(rs.getString("subject"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setResolvedAt(rs.getTimestamp("resolved_at"));
        return c;
    }
}
