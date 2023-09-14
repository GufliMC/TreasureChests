package com.guflimc.treasurechests.spigot.data.beans;

import com.guflimc.treasurechests.spigot.data.converters.ItemStackConverter;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbForeignKey;
import org.bukkit.inventory.ItemStack;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "treasure_loot")
public class BTreasureLoot extends BModel {

    @Id
    private UUID id;

    @ManyToOne(targetEntity = BTreasureChest.class)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    @Column(nullable = false)
    @JoinColumn(name = "chest_id")
    public final BTreasureChest chest;

    @Convert(converter = ItemStackConverter.class)
    @Column(length = 65535, columnDefinition = "TEXT", nullable = false)
    private final ItemStack item;

    @Column(nullable = false)
    @DbDefault("100")
    private int chance = 100;

    public BTreasureLoot(BTreasureChest chest, ItemStack item) {
        this.chest = chest;
        this.item = item;
    }

    @Override
    public boolean delete() {
        chest.removeLoot(this);
        return super.delete();
    }

    // getters

    public ItemStack item() {
        return item;
    }

    public int chance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

}
