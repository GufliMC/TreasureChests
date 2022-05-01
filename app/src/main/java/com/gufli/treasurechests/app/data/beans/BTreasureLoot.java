package com.gufli.treasurechests.app.data.beans;

import com.gufli.treasurechests.app.data.converters.ItemStackConverter;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "treasure_loot")
public class BTreasureLoot extends BModel {

    @Id
    private UUID id;

    @ManyToOne(targetEntity = BTreasureChest.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    public BTreasureChest chest;

    @Convert(converter = ItemStackConverter.class)
    private final ItemStack item;

    private final double chance;

    public BTreasureLoot(BTreasureChest chest, ItemStack item, double chance) {
        this.chest = chest;
        this.item = item;
        this.chance = chance;
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

    public double chance() {
        return chance;
    }

    //

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BTreasureLoot bl) && bl.id.equals(id);
    }

}
