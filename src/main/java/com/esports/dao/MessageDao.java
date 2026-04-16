package com.esports.dao;

import com.esports.db.DatabaseConnection;
import com.esports.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageDao {

    private final Connection con = DatabaseConnection.getInstance().getConnection();

    private void init() {
        MessagesForumMetadata.ensureLoaded(con);
    }

    public List<Message> getBySujet(int sujetId) {
        init();
        List<Message> list = new ArrayList<>();
        String qId = MessagesForumMetadata.qId();
        String qFk = MessagesForumMetadata.qFk();
        String qContent = MessagesForumMetadata.qContent();
        String sql = "SELECT * FROM messages_forum WHERE " + qFk + " = ? ORDER BY " + qId;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sujetId);
            ResultSet rs = ps.executeQuery();
            String fkName = MessagesForumMetadata.fkSujetColumn();
            String contentName = MessagesForumMetadata.contentColumn();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt(MessagesForumMetadata.idColumn()));
                m.setSujetId(rs.getInt(fkName));
                m.setContenu(rs.getString(contentName));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void add(Message m) {
        init();
        String qFk = MessagesForumMetadata.qFk();
        String qContent = MessagesForumMetadata.qContent();
        String authorCol = MessagesForumMetadata.authorColumn();
        String createdAtCol = MessagesForumMetadata.createdAtColumn();
        String likesCol = MessagesForumMetadata.firstExistingColumn("nombre_likes", "likes_count", "nb_likes");
        boolean hasAuthor = authorCol != null && !authorCol.isBlank();
        boolean hasCreatedAt = createdAtCol != null && !createdAtCol.isBlank();
        boolean hasLikes = likesCol != null && !likesCol.isBlank();

        StringBuilder cols = new StringBuilder(qFk).append(", ").append(qContent);
        StringBuilder vals = new StringBuilder("?, ?");
        if (hasAuthor) {
            cols.append(", ").append(MessagesForumMetadata.qAuthor());
            vals.append(", ?");
        }
        if (hasCreatedAt) {
            cols.append(", ").append(MessagesForumMetadata.qCreatedAt());
            vals.append(", NOW()");
        }
        if (hasLikes) {
            cols.append(", ").append(MessagesForumMetadata.q(likesCol));
            vals.append(", 0");
        }

        String sql = "INSERT INTO messages_forum (" + cols + ") VALUES (" + vals + ")";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, m.getSujetId());
            ps.setString(i++, m.getContenu());
            if (hasAuthor) {
                // Fallback auteur technique en l'absence d'authentification branchée.
                ps.setInt(i, 1);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Message m) {
        init();
        String qId = MessagesForumMetadata.qId();
        String qFk = MessagesForumMetadata.qFk();
        String qContent = MessagesForumMetadata.qContent();
        String sql = "UPDATE messages_forum SET " + qContent + " = ? WHERE " + qId + " = ? AND " + qFk + " = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getContenu());
            ps.setInt(2, m.getId());
            ps.setInt(3, m.getSujetId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        init();
        String qId = MessagesForumMetadata.qId();
        String sql = "DELETE FROM messages_forum WHERE " + qId + " = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Liste admin : messages + titre du sujet. */
    public List<AdminForumMessageRow> getAllForAdmin() {
        init();
        List<AdminForumMessageRow> list = new ArrayList<>();
        String qId = MessagesForumMetadata.qId();
        String qFk = MessagesForumMetadata.qFk();
        String qContent = MessagesForumMetadata.qContent();
        String fkName = MessagesForumMetadata.fkSujetColumn();
        String contentName = MessagesForumMetadata.contentColumn();

        String sql = "SELECT m.* , s.titre AS sujet_titre " +
                "FROM messages_forum m LEFT JOIN sujets_forum s ON m." + qFk + " = s.id " +
                "ORDER BY m." + qId + " DESC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AdminForumMessageRow row = new AdminForumMessageRow();
                row.id = rs.getInt(MessagesForumMetadata.idColumn());
                row.sujetId = rs.getInt(fkName);
                row.sujetTitre = rs.getString("sujet_titre");
                row.contenu = rs.getString(contentName);
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Stats admin : titre sujet -> nombre de messages. */
    public Map<String, Integer> countBySujetForAdmin() {
        init();
        Map<String, Integer> map = new LinkedHashMap<>();
        String qFk = MessagesForumMetadata.qFk();
        String sql = "SELECT COALESCE(s.titre, CONCAT('Sujet #', m." + qFk + ")) AS label, COUNT(*) AS c " +
                "FROM messages_forum m LEFT JOIN sujets_forum s ON m." + qFk + " = s.id " +
                "GROUP BY label ORDER BY c DESC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("label"), rs.getInt("c"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static class AdminForumMessageRow {
        public int id;
        public int sujetId;
        public String sujetTitre;
        public String contenu;
    }
}
