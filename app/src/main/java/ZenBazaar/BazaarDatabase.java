package ZenBazaar;

import java.sql.*;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class BazaarDatabase {
    private final JavaPlugin plugin;
    private Connection connection;

    public BazaarDatabase(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) return;
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/bazaar.db");
            setupTables();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not open SQLite connection", e);
        }
    }

    private void setupTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS bazaar_items (item TEXT PRIMARY KEY, supply INTEGER, demand INTEGER, price REAL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not close SQLite connection", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
