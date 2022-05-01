package com.gufli.treasurechests.app.session;

import com.gufli.treasurechests.app.data.beans.BTreasureChestInventory;
import org.bukkit.inventory.Inventory;

import java.time.Instant;

public class PlayerSession {

    public final Instant createdAt = Instant.now();
    public final PlayerSessionType type;

    // ADD_LOOT only
    public double chance;

    // INSPECT only
    public Inventory inventory;

    // LOOTING only
    public BTreasureChestInventory treasure_inventory;

    // CREATE only
    public boolean global;
    public int respawnMinutes;

    public PlayerSession(PlayerSessionType type) {
        this.type = type;
    }

    public PlayerSession(boolean global, int respawnMinutes) {
        this(PlayerSessionType.CREATE);
        this.global = global;
        this.respawnMinutes = respawnMinutes;
    }

    public PlayerSession(double chance) {
        this(PlayerSessionType.ADD_LOOT);
        this.chance = chance;
    }

    public PlayerSession(BTreasureChestInventory inv) {
        this(PlayerSessionType.LOOTING);
        this.treasure_inventory = inv;
    }

    public enum PlayerSessionType {
        CREATE, ADD_LOOT, INSPECT, LOOTING
    }

}
