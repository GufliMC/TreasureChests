package com.gufli.treasurechests.app.data.beans;

import com.gufli.treasurechests.app.data.converters.InventoryConverter;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;
import org.bukkit.inventory.Inventory;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "treasure_chest_inventories")
public class BTreasureChestInventory extends BModel {

    @Id
    private UUID id;

    private final UUID playerId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = BTreasureChest.class)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private final BTreasureChest chest;

    private final Instant time;

    @Convert(converter = InventoryConverter.class)
    private Inventory inventory;

    public BTreasureChestInventory(UUID playerId, BTreasureChest chest, Inventory inventory) {
        this.playerId = playerId;
        this.chest = chest;
        this.inventory = inventory;
        this.time = Instant.now();
    }

    // getters

    public UUID playerId() {
        return playerId;
    }

    public BTreasureChest chest() {
        return chest;
    }

    public Inventory inventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Instant time() {
        return time;
    }

    //

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BTreasureChestInventory pa) && pa.id.equals(id);
    }

}
