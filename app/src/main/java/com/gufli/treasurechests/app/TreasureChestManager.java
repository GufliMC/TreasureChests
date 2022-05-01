package com.gufli.treasurechests.app;

import com.gufli.treasurechests.app.data.DatabaseContext;
import com.gufli.treasurechests.app.data.beans.BModel;
import com.gufli.treasurechests.app.data.beans.BTreasureChest;
import com.gufli.treasurechests.app.data.beans.BTreasureChestInventory;
import com.gufli.treasurechests.app.data.beans.BTreasureLoot;
import com.gufli.treasurechests.app.data.beans.query.QBTreasureChest;
import com.gufli.treasurechests.app.data.beans.query.QBTreasureChestInventory;
import com.gufli.treasurechests.app.session.PlayerSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TreasureChestManager {

    private final DatabaseContext databaseContext;

    private final Set<BTreasureChest> chests = new HashSet<>();
    private final Set<BTreasureChestInventory> inventories = new HashSet<>();

    private final Map<Player, PlayerSession> sessions = new HashMap<>();

    private final Random random = new Random();

    public TreasureChestManager(DatabaseContext databaseContext) {
        this.databaseContext = databaseContext;
        reload();
    }

    void shutdown() {
        chests.clear();
        inventories.clear();
        sessions.clear();
    }

    void reload() {
        shutdown();

        // load chests
        chests.addAll(new QBTreasureChest().findSet());

        // load inventories of global chest
        chests.stream().filter(BTreasureChest::global)
                .forEach(chest -> inventories.add(new QBTreasureChestInventory()
                        .chest.eq(chest)
                        .setMaxRows(1)
                        .orderBy().time.desc()
                        .findOne()));

        // load players
        Bukkit.getOnlinePlayers().forEach(this::load);
    }

    public void load(Player player) {
        inventories.addAll(new QBTreasureChestInventory().playerId.eq(player.getUniqueId()).findSet());
    }

    public void unload(Player player) {
        inventories.removeIf(inv -> inv.playerId().equals(player.getUniqueId()));
        sessions.remove(player);
    }

    // session

    public void setSession(Player player, PlayerSession session) {
        sessions.put(player, session);
    }

    public void removeSession(Player player) {
        sessions.remove(player);
    }

    public PlayerSession session(Player player) {
        return sessions.get(player);
    }

    //

    public Inventory inventoryFor(Block block, Player player) {
        BTreasureChest chest = chestAt(block);
        if (chest == null) {
            return null;
        }

        BTreasureChestInventory tci = inventories.stream()
                .filter(inv -> inv.chest().equals(chest))
                .filter(inv -> chest.global() || inv.playerId().equals(player.getUniqueId()))
                .filter(inv -> inv.time().isAfter(Instant.now().minus(chest.respawnMinutes(), ChronoUnit.MINUTES)))
                .max(Comparator.comparing(BTreasureChestInventory::time))
                .orElse(null);

        if (tci != null) {
            setSession(player, new PlayerSession(tci));
            return tci.inventory();
        }

        List<ItemStack> items = new ArrayList<>();
        for (BTreasureLoot loot : chest.loot()) {
            if (loot.chance() >= 1) {
                items.add(loot.item().clone());
                continue;
            }

            int amount = 0;
            for (int i = 0; i < loot.item().getAmount(); i++) {
                if (random.nextDouble() < loot.chance()) {
                    amount++;
                }
            }

            if (amount > 0) {
                ItemStack item = loot.item().clone();
                item.setAmount(amount);
                items.add(item);
            }
        }

        int size = 27;
        if (block.getState() instanceof DoubleChest) {
            size = 54;
        }

        Inventory inv = Bukkit.createInventory(null, size, "Looterss");
        for (int i = 0; i < items.size(); i++) {
            inv.setItem(i, items.get(i));
        }

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
            }
        }

        return databaseContext.deleteAsync(models);
    }

    // chests

    public Collection<BTreasureChest> chests() {
        return Collections.unmodifiableSet(chests);
    }

    public BTreasureChest chestAt(Block block) {
        if (block.getState() instanceof DoubleChest dc) {
            return chestAt(List.of(
                    ((DoubleChest) dc.getLeftSide()).getLocation().getBlock(),
                    ((DoubleChest) dc.getRightSide()).getLocation().getBlock()
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

    public CompletableFuture<BTreasureChest> addChest(Location location, boolean global, int respawnMinutes) {
        BTreasureChest chest = new BTreasureChest(location, global, respawnMinutes);
        return databaseContext.saveAsync(chest).thenApply((v) -> {
            chests.add(chest);
            return chest;
        });
    }

}
