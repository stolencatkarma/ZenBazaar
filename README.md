# ZenBazaar

A dynamic, supply-and-demand-driven global bazaar plugin for PaperMC 1.21.1+ (Java 21). Players can buy and sell items using your server's economy (Vault required). Prices automatically adjust based on market activity, and server admins can set base prices and pricing models.

## Features
- `/buy` and `/sell` commands for trading the item in your hand
- Dynamic pricing: prices rise as supply drops and fall as supply increases
- Configurable base prices for all items
- Supports both supply/demand and fixed price models
- SQLite database for persistent market data
- Prevents players from buying and selling the same item on the same day
- Fully integrates with Vault for economy support
- Modern Adventure API messaging

## Installation
1. Build the plugin with Gradle: `gradlew build`
2. Copy `app/build/libs/ZenBazaar.jar` to your server's `plugins` folder
3. Ensure you have Vault and an economy plugin installed
4. Start or reload your PaperMC server

## Configuration
Edit `plugins/ZenBazaar/config.yml` to set base prices, pricing model, and more:

```yaml
base-prices:
  DIAMOND: 20.0
  IRON_INGOT: 5.0
  GOLD_INGOT: 9.0
  EMERALD: 1.0

pricing-model: SUPPLY_DEMAND # Options: SUPPLY_DEMAND, FIXED_PRICE
```
- **base-prices**: Set the starting price for any item.
- **pricing-model**: `SUPPLY_DEMAND` (dynamic) or `FIXED_PRICE` (static).

## Usage
- `/buy` — Buy the item you are holding from the bazaar
- `/sell` — Sell the item you are holding to the bazaar
- Players cannot buy and sell the same item on the same day
- Prices are always set by the market, not by players

## Requirements
- PaperMC 1.21.1+
- Java 21+
- [Vault](https://www.spigotmc.org/resources/vault.34315/) and a compatible economy plugin

## License
MIT

---

Contributions and suggestions welcome!
