package com.guflimc.treasurechests.spigot;

import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import com.guflimc.treasurechests.spigot.data.beans.*;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
    public final JavaPlugin plugin;

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
        chests.addAll(databaseContext.findAllAsync(BTreasureChest.class).join());

        // load players
        Bukkit.getOnlinePlayers().forEach(this::load);
    }

    public void load(Player player) {
        databaseContext.findAllWhereAsync(BTreasureChestInventory.class, "playerId", player.getUniqueId())
                .thenAccept(inventories::addAll);
    }

    public void unload(Player player) {
        inventories.removeIf(inv -> inv.playerId().equals(player.getUniqueId()));
    }

    public void save(Player player, Inventory inventory) {
        BTreasureChestInventory tci = inventories.stream()
                .filter(inv -> inv.playerId().equals(player.getUniqueId()))
                .filter(inv -> inv.inventory().equals(inventory))
                .findFirst().orElse(null);
        if (tci == null) {
            return;
        }

        tci.setInventory(inventory);
        save(tci);
    }

    public void refill(BTreasureChest chest) {
        inventories.removeIf(inv -> inv.chest().equals(chest));

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
                .filter(inv -> chest.respawnTime() <= 0 || inv.createdAt().isAfter(Instant.now().minus(chest.respawnTime(), ChronoUnit.SECONDS)))
                .max(Comparator.comparing(BTreasureChestInventory::createdAt))
                .orElse(null);

        // return inventory if it already exists
        if (tci != null) {
            return tci.inventory();
        }

        // get items and randomize winning chances
        List<ItemStack> items = new ArrayList<>();
        for (BTreasureLoot loot : chest.loot()) {
            if (loot.chance() >= 100) {
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
        int size = isDoubleChest(block) ? 54 : 27;
        String title = BukkitComponentSerializer.legacy().serialize(chest.title());
        Inventory inv = Bukkit.createInventory(null, size, title);

        // fill chess with spread
        if (chest.splitStacks()) {
            spread(items, inv.getSize());
        }

        // put items in inventory
        fill(items, inv);

        // save current inventory
        BTreasureChestInventory ntci = new BTreasureChestInventory(player.getUniqueId(), chest, inv);
        inventories.add(ntci);
        save(ntci);

        return inv;
    }

    private void spread(List<ItemStack> items, int invSize) {
        // spread
        outer:
        while (items.size() < (int) (invSize * 0.75)) {
            Collections.shuffle(items);
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item.getAmount() == 1) {
                    continue;
                }

                int amount = item.getAmount();
                int half = amount / 2;
                item.setAmount(amount - half);

                ItemStack clone = item.clone();
                clone.setAmount(half);
                items.add(clone);

                continue outer;
            }

            break;
        }
    }

    private void fill(List<ItemStack> items, Inventory inv) {
        Set<Integer> indexes = new HashSet<>();
        for (ItemStack item : items) {
            // generate random index in inventory
            int index;
            do {
                index = random.nextInt(inv.getSize());
            } while (indexes.contains(index));
            indexes.add(index);

            // set item at index
            inv.setItem(index, item);

            if (indexes.size() == inv.getSize()) {
                return;
            }
        }
    }

    // DATABASE

    public CompletableFuture<Void> save(BModel... models) {
        return databaseContext.persistAsync(models);
    }

    public CompletableFuture<Void> delete(BModel... models) {
        for (BModel m : models) {
            if (m instanceof BTreasureChest btc) {
                chests.remove(btc);
            } else if (m instanceof BTreasureLoot btl) {
                btl.chest.removeLoot(btl);
            } else if (m instanceof BTreasureChestInventory btci) {
                inventories.remove(btci);
            }
        }

        return databaseContext.removeAsync(models);
    }

    // chests

    public Collection<BTreasureChest> chests() {
        return Collections.unmodifiableSet(chests);
    }

    public boolean isDoubleChest(Location location) {
        return isDoubleChest(location.getBlock());
    }

    public boolean isDoubleChest(Block block) {
        return block.getState() instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory;
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
        return databaseContext.persistAsync(chest).thenCompose((v) -> {
            chests.add(chest);
            CompletableFuture<BTreasureChest> cf = new CompletableFuture<>();
            plugin.getServer().getScheduler().runTask(plugin, () -> cf.complete(chest));
            return cf;
        });
    }

}
