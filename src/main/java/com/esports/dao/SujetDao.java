package com.esports.dao;

import com.esports.db.DatabaseConnection;
import com.esports.models.Sujet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SujetDao {
    private Connection con = DatabaseConnection.getInstance().getConnection();

    public List<Sujet> getAll() {
        List<Sujet> list = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM sujets_forum")) {
            while (rs.next()) {
                Sujet s = new Sujet();
                s.setId(rs.getInt("id"));
                s.setTitre(rs.getString("titre"));
                s.setContenu(rs.getString("contenu"));
                list.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Sujet getById(int id) {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM sujets_forum WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Sujet s = new Sujet();
                s.setId(rs.getInt("id"));
                s.setTitre(rs.getString("titre"));
                s.setContenu(rs.getString("contenu"));
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void add(Sujet s) {
        String sql = "INSERT INTO sujets_forum (titre, contenu, cree_par, categorie, date_creation, est_verrouille) VALUES (?, ?, 'Hassen', 'General', NOW(), ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTitre());
            ps.setString(2, s.getContenu());
            ps.setBoolean(3, false);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void update(Sujet s) {
        String sql = "UPDATE sujets_forum SET titre = ?, contenu = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTitre());
            ps.setString(2, s.getContenu());
            ps.setInt(3, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        MessagesForumMetadata.ensureLoaded(con);
        String qFk = MessagesForumMetadata.qFk();
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM messages_forum WHERE " + qFk + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM sujets_forum WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
