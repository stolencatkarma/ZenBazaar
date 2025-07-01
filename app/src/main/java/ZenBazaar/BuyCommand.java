
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

public class BuyCommand implements CommandExecutor {
    private final ZenBazaarPlugin plugin;

    public BuyCommand(ZenBazaarPlugin plugin) {
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
            player.sendMessage(Component.text("Hold an item to buy from the bazaar.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        // Bazaar buy logic with Vault economy
        String itemName = item.getType().name();
        int amount = item.getAmount();
        BazaarDatabase db = plugin.getDatabase();
        Economy econ = plugin.getEconomy();
        if (econ == null) {
            player.sendMessage(Component.text("Economy plugin not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        // Safety check: prevent buy if player sold this item today
        if (plugin.getHistory().hasActionToday(player.getUniqueId(), itemName, "SELL")) {
            player.sendMessage(Component.text("You cannot buy this item on the same day you sold it.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        try (Connection conn = db.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT price, supply, demand FROM bazaar_items WHERE item = ?");
            ps.setString(1, itemName);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                player.sendMessage(Component.text("This item is not available in the bazaar.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
            double price = rs.getDouble("price");
            int buyAmount = item.getMaxStackSize();
            double totalCost = price * buyAmount;
            if (econ.getBalance(player) < totalCost) {
                player.sendMessage(Component.text("You need $" + totalCost + " to buy this.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
            // Withdraw money
            econ.withdrawPlayer(player, totalCost);
            // Give item
            ItemStack bought = new ItemStack(item.getType(), buyAmount);
            player.getInventory().addItem(bought);
            // Update supply/demand/price or use fixed price
            if (plugin.getPricingModel().equalsIgnoreCase("SUPPLY_DEMAND")) {
                PreparedStatement update = conn.prepareStatement("UPDATE bazaar_items SET supply = supply - ?, demand = demand + 1, price = price * 1.02 WHERE item = ?");
                update.setInt(1, buyAmount);
                update.setString(2, itemName);
                update.executeUpdate();
            }
            // Record buy action
            plugin.getHistory().recordAction(player.getUniqueId(), itemName, "BUY");
            player.sendMessage(Component.text("You bought " + buyAmount + " " + itemName + " for $" + totalCost + "!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while buying from the bazaar.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            e.printStackTrace();
        }
        return true;
    }
}
