package com.gufli.treasurechests.app.listeners;

import com.gufli.treasurechests.app.TreasureChestManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class PlayerChestListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerChestListener(TreasureChestManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        Inventory inv = manager.inventoryFor(block, player);
        if (inv == null) {
            return;
        }

        event.setCancelled(true);
        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        if ( event.getPlayer() instanceof Player player ) {
            manager.save(player, event.getInventory());
        }
    }

}
