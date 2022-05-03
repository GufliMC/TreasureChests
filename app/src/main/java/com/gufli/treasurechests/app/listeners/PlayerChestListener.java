package com.gufli.treasurechests.app.listeners;

import com.gufli.treasurechests.app.TreasureChestManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class PlayerChestListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerChestListener(TreasureChestManager manager) {
        this.manager = manager;
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
        Inventory inv = manager.inventoryFor(block, event.getPlayer());
        if (inv == null) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().openInventory(inv);

        // TODO also save inventory on close
    }

}
