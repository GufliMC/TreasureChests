package com.gufli.treasurechests.app.data.beans;

import com.gufli.treasurechests.app.data.converters.InventoryConverter;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.bukkit.inventory.Inventory;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "treasure_chest_inventories")
public class BTreasureChestInventory extends BModel {

    @Id
    private UUID id;

    @Column(nullable = false)
    private final UUID playerId;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = BTreasureChest.class)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    @Column(nullable = false)
    private final BTreasureChest chest;

    @Convert(converter = InventoryConverter.class)
    @Column(length = 65535, columnDefinition = "TEXT", nullable = false)
    private final Inventory inventory;

    @WhenCreated
    Instant createdAt;

    @WhenModified
    Instant updatedAt;

    public BTreasureChestInventory(UUID playerId, BTreasureChest chest, Inventory inventory) {
        this.playerId = playerId;
        this.chest = chest;
        this.inventory = inventory;
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

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
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
