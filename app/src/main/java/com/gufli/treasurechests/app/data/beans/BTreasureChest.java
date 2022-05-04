package com.gufli.treasurechests.app.data.beans;

import com.gufli.treasurechests.app.data.converters.LocationConverter;
import io.ebean.annotation.DbDefault;
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

    //

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BTreasureChest btc) && btc.id.equals(id);
    }
}
