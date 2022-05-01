package com.gufli.treasurechests.app.listeners;

import com.gufli.treasurechests.app.TreasureChestManager;
import com.gufli.treasurechests.app.data.beans.BTreasureChest;
import com.gufli.treasurechests.app.data.beans.BTreasureLoot;
import com.gufli.treasurechests.app.session.PlayerSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PlayerChestListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerChestListener(TreasureChestManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().name().contains("CHEST")) {
            return;
        }

        BTreasureChest chest = manager.chestAt(event.getBlock());
        if (chest == null) {
            return;
        }

        manager.delete(chest);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }


        Player player = event.getPlayer();
        PlayerSession session = manager.session(player);
        if (session == null) {
            Inventory inv = manager.inventoryFor(block, event.getPlayer());
            if (inv == null) {
                return;
            }

            event.setCancelled(true);
            event.getPlayer().openInventory(inv);
            return;
        }

        event.setCancelled(true);
        if (session.createdAt.isBefore(Instant.now().minus(60, ChronoUnit.MINUTES))) {
            return;
        }

        if (session.type == PlayerSession.PlayerSessionType.CREATE) {
            manager.addChest(block.getLocation(), session.global, session.respawnMinutes)
                    .thenAccept(chest -> {
                        player.sendMessage(ChatColor.GREEN + "Treasure chest created.");
                        manager.removeSession(player);
                    });
            return;
        }

        BTreasureChest chest = manager.chestAt(block);
        if (chest == null) {
            return;
        }

        if (session.type == PlayerSession.PlayerSessionType.ADD_LOOT) {
            BTreasureLoot loot = chest.addLoot(player.getItemInHand(), session.chance);
            manager.save(loot);

            player.sendMessage(ChatColor.GREEN + "Loot added to chest.");
            manager.removeSession(player);
            return;
        }

        if (session.type == PlayerSession.PlayerSessionType.INSPECT) {
            Inventory inv = Bukkit.createInventory(null, 54);
            // TODO better inventory framework
            for (int i = 0; i < chest.loot().size(); i++) {
                BTreasureLoot loot = chest.loot().get(i);
                ItemStack item = loot.item().clone();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GRAY + "Chance: " + ChatColor.GOLD + (loot.chance() * 100) + "%");
//                lore.add(ChatColor.GRAY + "Right-click to remove."); // TODO
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }

            player.openInventory(inv);
            session.inventory = inv;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerSession session = manager.session(player);
        if (session == null || session.inventory == null) {
            return;
        }

        if (!event.getView().getTopInventory().equals(session.inventory)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerSession session = manager.session(player);
        if (session == null) {
            return;
        }

        if (session.type == PlayerSession.PlayerSessionType.INSPECT) {
            manager.setSession(player, null);
            return;
        }

        if (session.type == PlayerSession.PlayerSessionType.LOOTING) {
            session.treasure_inventory.setInventory(event.getInventory());
            manager.save(session.treasure_inventory);
            manager.setSession(player, null);
            return;
        }
    }

}
