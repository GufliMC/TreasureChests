package com.guflimc.treasurechests.spigot.listeners;

import com.guflimc.brick.i18n.api.time.DurationFormatter;
import com.guflimc.brick.i18n.spigot.api.SpigotTranslator;
import com.guflimc.treasurechests.spigot.TreasureChestManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.time.Duration;

public class PlayerChestListener implements Listener {

    private final TreasureChestManager manager;
    private final SpigotTranslator translator;

    public PlayerChestListener(TreasureChestManager manager, SpigotTranslator translator) {
        this.manager = manager;
        this.translator = translator;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !manager.isTreasureChestType(block.getType())) {
            return;
        }

        Player player = event.getPlayer();
        Inventory inv = manager.inventoryFor(block, player);
        if (inv == null) {
            return;
        }

        event.setCancelled(true);
        if ( inv.isEmpty() ) {
            Duration duration = manager.respawnIn(block, player);
            if ( duration == null ) {
                translator.send(player, "chest.looted");
                return;
            }

            String formatted = DurationFormatter.COMPACT.format(duration);
            translator.send(player, "chest.looted.duration", formatted);
            return;
        }

        player.openInventory(inv);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        if ( event.getPlayer() instanceof Player player ) {
            manager.save(player, event.getInventory());
        }
    }

}
