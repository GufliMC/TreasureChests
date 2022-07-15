package com.guflimc.treasurechests.spigot.data.beans;

import com.gufli.dbeantools.adventure.converters.ComponentConverter;
import com.guflimc.treasurechests.spigot.data.converters.LocationConverter;
import io.ebean.annotation.DbDefault;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "treasure_chests")
public class BTreasureChest extends BModel {

    @Id
    private UUID id;

    @Convert(converter = LocationConverter.class)
    @Column(nullable = false)
    private final Location location;

    @OneToMany(targetEntity = BTreasureLoot.class, mappedBy = "chest")
    private List<BTreasureLoot> loot;

    @Column(nullable = false)
    @DbDefault("3600")
    private int respawnTime = 3600;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @DbDefault("PLAYER_BOUND")
    private ChestMode mode = ChestMode.PLAYER_BOUND;

    @Column(nullable = false)
    @DbDefault("true")
    private boolean splitStacks = true;

    @Column(length = 4096)
    @Convert(converter = ComponentConverter.class, attributeName = "title")
    private Component title;

    public BTreasureChest(Location location) {
        this.location = location;
    }

    // getters

    public UUID id() {
        return id;
    }

    public Location location() {
        return location;
    }

    public int respawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(int respawnTime) {
        respawnTime = Math.max(0, respawnTime);
        this.respawnTime = respawnTime;
    }

    public ChestMode mode() {
        return mode;
    }

    public void setChestMode(ChestMode mode) {
        this.mode = mode;
    }

    // loot

    public List<BTreasureLoot> loot() {
        return loot;
    }

    public BTreasureLoot addLoot(ItemStack item) {
        BTreasureLoot loot = new BTreasureLoot(this, item);
        this.loot.add(loot);
        return loot;
    }

    public void removeLoot(BTreasureLoot loot) {
        this.loot.remove(loot);
    }

    public boolean splitStacks() {
        return splitStacks;
    }

    public void setSplitStacks(boolean splitStacks) {
        this.splitStacks = splitStacks;
    }

    public Component title() {
        if ( title == null ) {
            return Component.text("Treasure Chest");
        }
        return title;
    }

    public void setTitle(Component title) {
        this.title = title;
    }
}
