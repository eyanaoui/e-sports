package com.esports.dao;

import com.esports.db.DatabaseConnection;
import com.esports.models.Tournament;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {
    private Connection con = DatabaseConnection.getInstance().getConnection();

    public List<Tournament> getAll() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament ORDER BY created_at DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTournament(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void add(Tournament t) {
        String sql = "INSERT INTO tournament (name, game, description, format, max_teams, start_date, end_date, registration_deadline, status, prize, rules, organizer_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            prepareStatement(ps, t);
            ps.executeUpdate();
            System.out.println("✅ Tournament added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Tournament t) {
        String sql = "UPDATE tournament SET name=?, game=?, description=?, format=?, max_teams=?, " +
                "start_date=?, end_date=?, registration_deadline=?, status=?, prize=?, rules=?, " +
                "organizer_id=?, updated_at=NOW() WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            prepareStatement(ps, t);
            ps.setInt(13, t.getId()); // ID for the WHERE clause
            ps.executeUpdate();
            System.out.println("✅ Tournament updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM tournament WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Tournament deleted!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void prepareStatement(PreparedStatement ps, Tournament t) throws SQLException {
        ps.setString(1, t.getName());
        ps.setString(2, t.getGame());
        ps.setString(3, t.getDescription());
        ps.setString(4, t.getFormat());
        ps.setInt(5, t.getMax_teams());
        ps.setTimestamp(6, t.getStart_date() != null ? Timestamp.valueOf(t.getStart_date()) : null);
        ps.setTimestamp(7, t.getEnd_date() != null ? Timestamp.valueOf(t.getEnd_date()) : null);
        ps.setTimestamp(8, t.getRegistration_deadline() != null ? Timestamp.valueOf(t.getRegistration_deadline()) : null);
        ps.setString(9, t.getStatus());
        ps.setString(10, t.getPrize());
        ps.setString(11, t.getRules());
        ps.setInt(12, t.getOrganizer_id());
    }

    private Tournament mapResultSetToTournament(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setGame(rs.getString("game"));
        t.setDescription(rs.getString("description"));
        t.setFormat(rs.getString("format"));
        t.setMax_teams(rs.getInt("max_teams"));

        if (rs.getTimestamp("start_date") != null)
            t.setStart_date(rs.getTimestamp("start_date").toLocalDateTime());
        if (rs.getTimestamp("end_date") != null)
            t.setEnd_date(rs.getTimestamp("end_date").toLocalDateTime());
        if (rs.getTimestamp("registration_deadline") != null)
            t.setRegistration_deadline(rs.getTimestamp("registration_deadline").toLocalDateTime());

        t.setStatus(rs.getString("status"));
        t.setPrize(rs.getString("prize"));
        t.setRules(rs.getString("rules"));
        t.setOrganizer_id(rs.getInt("organizer_id"));
        return t;
    }
}