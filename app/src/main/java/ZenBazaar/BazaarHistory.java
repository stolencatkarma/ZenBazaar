package ZenBazaar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class BazaarHistory {
    private final BazaarDatabase db;

    public BazaarHistory(BazaarDatabase db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS bazaar_history (uuid TEXT, item TEXT, date TEXT, action TEXT, PRIMARY KEY(uuid, item, date, action))")) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasActionToday(UUID uuid, String item, String action) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM bazaar_history WHERE uuid = ? AND item = ? AND date = ? AND action = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, item);
            ps.setString(3, LocalDate.now().toString());
            ps.setString(4, action);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void recordAction(UUID uuid, String item, String action) {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO bazaar_history (uuid, item, date, action) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, item);
            ps.setString(3, LocalDate.now().toString());
            ps.setString(4, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
