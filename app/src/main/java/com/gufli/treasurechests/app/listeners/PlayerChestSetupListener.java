package com.gufli.treasurechests.app.listeners;

import com.gufli.treasurechests.app.TreasureChestManager;
import com.gufli.treasurechests.app.data.beans.BTreasureChest;
import com.gufli.treasurechests.app.data.beans.BTreasureLoot;
import com.guflimc.mastergui.bukkit.BukkitMasterGUI;
import com.guflimc.mastergui.bukkit.api.IBukkitMenu;
import com.guflimc.mastergui.bukkit.builder.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerChestSetupListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerChestSetupListener(TreasureChestManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().name().contains("CHEST")) {
            return;
        }

        BTreasureChest chest = manager.chestAt(event.getBlock());
        if (chest != null) {
            event.setCancelled(true);
            // TODO send message
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !block.getType().name().contains("CHEST")) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("treasurechests.setup") || !player.isSneaking() || player.getInventory().getItem(EquipmentSlot.HAND) != null) {
            return;
        }

        BTreasureChest chest = manager.chestAt(block);
        if (chest == null) {
            create(event.getPlayer(), block);
            return;
        }

        info(event.getPlayer(), chest);
    }

    // MENUS

    private void create(Player player, Block block) {
        BukkitMasterGUI.builder()
                .withItem(
                        ItemStackBuilder.of(Material.NETHER_STAR).withName("&6Create Treasure Chest").build(),
                        (event) -> {
                            manager.addChest(block.getLocation()).thenAccept(chest -> {
                                player.sendMessage(ChatColor.GREEN + "Treasure chest created.");
                                player.closeInventory();
                            });
                            return true;
                        })
                .build().open(player);
    }

    private void info(Player player, BTreasureChest chest) {
        BukkitMasterGUI.builder()
                .withItem(
                        ItemStackBuilder.of(Material.GOLD_INGOT).withName("&6Inspect loot").build(),
                        (event) -> {
                            inspect(player, chest);
                            return true;
                        })
                .withItem(
                        ItemStackBuilder.of(Material.LAVA_BUCKET).withName("&6Delete chest").build(),
                        (event) -> {
                            delete(player, chest);
                            return true;
                        })
                .build().open(player);
    }

    private void delete(Player player, BTreasureChest chest) {
        BukkitMasterGUI.builder()
                .withItem(
                        ItemStackBuilder.of(Material.GREEN_TERRACOTTA).withName("&aConfirm").build(),
                        (event) -> {
                            manager.delete(chest);
                            player.sendMessage(ChatColor.GREEN + "Treasure chest deleted.");
                            player.closeInventory();
                            return true;
                        })
                .withItem(
                        ItemStackBuilder.of(Material.RED_TERRACOTTA).withName("&cCancel").build(),
                        (event) -> {
                            info(player, chest);
                            return true;
                        })
                .build().open(player);
    }

    private void inspect(Player player, BTreasureChest chest) {
        IBukkitMenu menu = BukkitMasterGUI.create(54, "Drop items to add loot.");
        for (int i = 0; i < chest.loot().size(); i++) {
            BTreasureLoot loot = chest.loot().get(i);
            menu.setItem(i,
                    ItemStackBuilder.of(loot.item()).withLore(
                            "",
                            ChatColor.GRAY + "Chance: " + ChatColor.GOLD + (loot.chance() * 100) + "%",
                            ChatColor.GRAY + "Right-click to remove."
                    ).build(),
                    (event) -> {
                        if ( event.getClick() == ClickType.RIGHT ) {
                            manager.delete(loot).thenRun(() -> inspect(player, chest));
                            return true;
                        }
                        return false;
                    });
        }

        menu.addClickListener(event -> {
            player.sendMessage("poggers");
        });

        menu.open(player);
    }

}
