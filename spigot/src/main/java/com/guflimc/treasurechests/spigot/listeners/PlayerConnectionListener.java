package com.guflimc.treasurechests.spigot.listeners;

import com.guflimc.treasurechests.spigot.TreasureChestManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerConnectionListener(TreasureChestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.unload(event.getPlayer());
    }
}
