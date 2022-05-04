package com.gufli.treasurechests.app;

import com.gufli.treasurechests.app.data.DatabaseContext;
import com.gufli.treasurechests.app.data.beans.*;
import com.gufli.treasurechests.app.data.beans.query.QBTreasureChest;
import com.gufli.treasurechests.app.data.beans.query.QBTreasureChestInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TreasureChestManager {

    private final DatabaseContext databaseContext;
    private final JavaPlugin plugin;

    private final Set<BTreasureChest> chests = new HashSet<>();
    private final Set<BTreasureChestInventory> inventories = new HashSet<>();

    private final Random random = new Random();

    public TreasureChestManager(JavaPlugin plugin, DatabaseContext databaseContext) {
        this.plugin = plugin;
        this.databaseContext = databaseContext;
        reload();
    }

    void shutdown() {
        chests.clear();
        inventories.clear();
    }

    void reload() {
        shutdown();

        // load chests
        chests.addAll(new QBTreasureChest().findSet());

        // load players
        Bukkit.getOnlinePlayers().forEach(this::load);
    }

    public void load(Player player) {
        inventories.addAll(new QBTreasureChestInventory().playerId.eq(player.getUniqueId()).findSet());
    }

    public void unload(Player player) {
        inventories.removeIf(inv -> inv.playerId().equals(player.getUniqueId()));
    }

    public void save(Player player, Inventory inventory) {
        inventories.stream()
                .filter(inv -> inv.playerId().equals(player.getUniqueId()))
                .filter(inv -> inv.inventory().equals(inventory))
                .findAny().ifPresent(this::save);
    }

    //

    public boolean isTreasureChestType(Material material) {
        return material.name().contains("CHEST") || material.name().contains("SHULKER_BOX");
    }

    public Inventory inventoryFor(Block block, Player player) {
        BTreasureChest chest = chestAt(block);
        if (chest == null) {
            return null;
        }

        BTreasureChestInventory tci = inventories.stream()
                .filter(inv -> inv.chest().equals(chest))
                .filter(inv -> inv.chest().mode() == ChestMode.SERVER_BOUND || inv.playerId().equals(player.getUniqueId()))
                .filter(inv -> inv.createdAt().isAfter(Instant.now().minus(chest.respawnTime(), ChronoUnit.SECONDS)))
                .max(Comparator.comparing(BTreasureChestInventory::createdAt))
                .orElse(null);

        // return inventory if it already exists
        if (tci != null) {
            return tci.inventory();
        }

        // get items and randomize winning chances
        List<ItemStack> items = new ArrayList<>();
        for (BTreasureLoot loot : chest.loot()) {
            if (loot.chance() >= 1) {
                items.add(loot.item().clone());
                continue;
            }

            int amount = 0;
            for (int i = 0; i < loot.item().getAmount(); i++) {
                if (random.nextInt(100) < loot.chance()) {
                    amount++;
                }
            }

            if (amount > 0) {
                ItemStack item = loot.item().clone();
                item.setAmount(amount);
                items.add(item);
            }
        }

        // get size of chest
        int size = 27;
        if (block.getState() instanceof DoubleChest) {
            size = 54;
        }

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "Treasure Chest");

        Set<Integer> indexes = new HashSet<>();
        for (ItemStack item : items) {
            // generate random index in inventory
            int index;
            do {
                index = random.nextInt(size);
            } while (indexes.contains(index));
            indexes.add(index);

            // set item at index
            inv.setItem(index, item);
        }

        // save current inventory
        BTreasureChestInventory ntci = new BTreasureChestInventory(player.getUniqueId(), chest, inv);
        save(ntci).thenRun(() -> inventories.add(ntci));

        return inv;
    }

    // DATABASE

    public CompletableFuture<Void> save(BModel... models) {
        return databaseContext.saveAsync(models);
    }

    public CompletableFuture<Void> delete(BModel... models) {
        for (BModel m : models) {
            if (m instanceof BTreasureChest btc) {
                chests.remove(btc);
            } else if (m instanceof BTreasureLoot btl) {
                btl.chest.removeLoot(btl);
            }
        }

        return databaseContext.deleteAsync(models);
    }

    // chests

    public Collection<BTreasureChest> chests() {
        return Collections.unmodifiableSet(chests);
    }

    public BTreasureChest chestAt(Block block) {
        if (block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory dci) {
            return chestAt(List.of(
                    dci.getLeftSide().getLocation().getBlock(),
                    dci.getRightSide().getLocation().getBlock()
            ));
        }

        return chestAt(List.of(block));
    }

    private BTreasureChest chestAt(Collection<Block> blocks) {
        return chests.stream()
                .filter(c -> blocks.stream().anyMatch(b ->
                        c.location().getWorld().getUID().equals(b.getWorld().getUID()) &&
                                c.location().getBlockX() == b.getX() &&
                                c.location().getBlockY() == b.getY() &&
                                c.location().getBlockZ() == b.getZ()))
                .findAny().orElse(null);
    }

    public CompletableFuture<BTreasureChest> addChest(Location location) {
        BTreasureChest chest = new BTreasureChest(location);
        return databaseContext.saveAsync(chest).thenCompose((v) -> {
            chests.add(chest);
            CompletableFuture<BTreasureChest> cf = new CompletableFuture<>();
            plugin.getServer().getScheduler().runTask(plugin, () -> cf.complete(chest));
            return cf;
        });
    }

}
