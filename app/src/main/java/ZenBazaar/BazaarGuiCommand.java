package ZenBazaar;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BazaarGuiCommand implements CommandExecutor, Listener {
    private final ZenBazaarPlugin plugin;
    private static final String GUI_TITLE = "Bazaar Market";

    public BazaarGuiCommand(ZenBazaarPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command."));
            return true;
        }
        Player player = (Player) sender;
        openBazaarGui(player);
        return true;
    }

    public void openBazaarGui(Player player) {
        List<ItemStack> items = new ArrayList<>();
        Connection conn = plugin.getDatabase().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT item, price FROM bazaar_items");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String itemName = rs.getString("item");
                double price = rs.getDouble("price");
                Material mat = Material.matchMaterial(itemName);
                if (mat == null) continue;
                ItemStack stack = new ItemStack(mat, 1);
                ItemMeta meta = stack.getItemMeta();
                meta.displayName(Component.text(itemName));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Price: $" + price));
                meta.lore(lore);
                stack.setItemMeta(meta);
                items.add(stack);
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("Error loading bazaar items."));
            e.printStackTrace();
            return;
        }
        int size = ((items.size() - 1) / 9 + 1) * 9;
        size = Math.max(9, Math.min(size, 54));
        Inventory inv = Bukkit.createInventory(null, size, Component.text(GUI_TITLE));
        for (int i = 0; i < items.size() && i < size; i++) {
            inv.setItem(i, items.get(i));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getView().title().equals(Component.text(GUI_TITLE))) {
            event.setCancelled(true);
            // Optionally: handle click-to-buy here
        }
    }
}
