package ZenBazaar;

import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ZenBazaarPlugin extends JavaPlugin {
    private BazaarDatabase database;
    private BazaarHistory history;
    private Economy economy;
    private java.util.Map<String, Double> basePrices;
    private String pricingModel;

    @Override
    public void onEnable() {
        // Setup Vault economy
        if (!setupEconomy()) {
            getLogger().severe("Vault and an economy plugin are required! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Load config and base prices
        saveDefaultConfig();
        basePrices = new java.util.HashMap<>();
        if (getConfig().isConfigurationSection("base-prices")) {
            for (String key : getConfig().getConfigurationSection("base-prices").getKeys(false)) {
                basePrices.put(key, getConfig().getDouble("base-prices." + key));
            }
        }
        pricingModel = getConfig().getString("pricing-model", "SUPPLY_DEMAND");
        // Initialize and open the database
        database = new BazaarDatabase(this);
        database.openConnection();
        history = new BazaarHistory(database);
        // Register commands
        getCommand("buy").setExecutor(new BuyCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getLogger().info("ZenBazaar enabled!");
    }

    public String getPricingModel() {
        return pricingModel;
    }


    @Override
    public void onDisable() {
        if (database != null) database.closeConnection();
        getLogger().info("ZenBazaar disabled!");
    }

    public BazaarDatabase getDatabase() {
        return database;
    }

    public Economy getEconomy() {
        return economy;
    }

    public BazaarHistory getHistory() {
        return history;
    }

    public Double getBasePrice(String itemName) {
        return basePrices.get(itemName);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
