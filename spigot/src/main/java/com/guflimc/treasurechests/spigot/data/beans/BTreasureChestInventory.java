package com.guflimc.treasurechests.spigot.data.beans;

import com.guflimc.treasurechests.spigot.data.converters.InventoryConverter;
import com.guflimc.treasurechests.spigot.util.DatabaseWrapper;
import io.ebean.Transaction;
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

    @ManyToOne(targetEntity = BTreasureChest.class)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    @Column(nullable = false)
    private final BTreasureChest chest;

    @Convert(converter = InventoryConverter.class)
    @Column(length = 65535, columnDefinition = "TEXT", nullable = false)
    private DatabaseWrapper<Inventory> inventory;

    @WhenCreated
    Instant createdAt;

    @WhenModified
    Instant updatedAt;

    public BTreasureChestInventory(UUID playerId, BTreasureChest chest, Inventory inventory) {
        this.playerId = playerId;
        this.chest = chest;
        this.inventory = new DatabaseWrapper<>(inventory);
    }

    @Override
    public void update(Transaction transaction) {
        transaction.setUpdateAllLoadedProperties(true);
        super.update(transaction);
    }

    // getters

    public UUID playerId() {
        return playerId;
    }

    public BTreasureChest chest() {
        return chest;
    }

    public Inventory inventory() {
        return inventory.value;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = new DatabaseWrapper<>(inventory);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}
