
package ZenBazaar;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.milkbowl.vault.economy.Economy;

public class SellCommand implements CommandExecutor {
    private final ZenBazaarPlugin plugin;

    public SellCommand(ZenBazaarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(Component.text("Hold an item to sell to the bazaar.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        // Bazaar sell logic with Vault economy
        String itemName = item.getType().name();
        int amount = item.getAmount();
        BazaarDatabase db = plugin.getDatabase();
        Economy econ = plugin.getEconomy();
        if (econ == null) {
            player.sendMessage(Component.text("Economy plugin not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        // Safety check: prevent sell if player bought this item today
        if (plugin.getHistory().hasActionToday(player.getUniqueId(), itemName, "BUY")) {
            player.sendMessage(Component.text("You cannot sell this item on the same day you bought it.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT price, supply, demand FROM bazaar_items WHERE item = ?")) {
            ps.setString(1, itemName);
            try (ResultSet rs = ps.executeQuery()) {
                double price;
                if (!rs.next()) {
                    // Insert new item with config base price and supply
                    Double configPrice = plugin.getBasePrice(itemName);
                    price = configPrice != null ? configPrice : 1.0;
                    try (PreparedStatement ins = conn.prepareStatement("INSERT INTO bazaar_items (item, supply, demand, price) VALUES (?, ?, ?, ?)")) {
                        ins.setString(1, itemName);
                        ins.setInt(2, amount * 10); // initial supply
                        ins.setInt(3, 0);
                        ins.setDouble(4, price);
                        ins.executeUpdate();
                    }
                } else {
                    price = rs.getDouble("price");
                }
                // Remove item from player
                item.setAmount(0);
                // Pay player
                double payout = price * amount;
                econ.depositPlayer(player, payout);
                // Update supply/demand/price or use fixed price
                if (plugin.getPricingModel().equalsIgnoreCase("SUPPLY_DEMAND")) {
                    try (PreparedStatement update = conn.prepareStatement("UPDATE bazaar_items SET supply = supply + ?, demand = demand - 1, price = price * 0.98 WHERE item = ?")) {
                        update.setInt(1, amount);
                        update.setString(2, itemName);
                        update.executeUpdate();
                    }
                }
                // Record sell action
                plugin.getHistory().recordAction(player.getUniqueId(), itemName, "SELL");
                player.sendMessage(Component.text("You sold " + amount + " " + itemName + " for $" + payout + "!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while selling to the bazaar.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            e.printStackTrace();
        }
        return true;
    }
}
