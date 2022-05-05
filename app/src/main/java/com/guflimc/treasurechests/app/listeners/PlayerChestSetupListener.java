package com.guflimc.treasurechests.app.listeners;

import com.guflimc.treasurechests.app.TreasureChestManager;
import com.guflimc.treasurechests.app.data.beans.BTreasureChest;
import com.guflimc.treasurechests.app.data.beans.BTreasureLoot;
import com.guflimc.treasurechests.app.data.beans.ChestMode;
import com.guflimc.mastergui.bukkit.BukkitMasterGUI;
import com.guflimc.mastergui.bukkit.api.IBukkitMenu;
import com.guflimc.mastergui.bukkit.item.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class PlayerChestSetupListener implements Listener {

    private final TreasureChestManager manager;

    public PlayerChestSetupListener(TreasureChestManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if ( !manager.isTreasureChestType(event.getBlock().getType()) ) {
            return;
        }

        BTreasureChest chest = manager.chestAt(event.getBlock());
        if (chest == null ) {
            return;
        }

        if ( !event.getPlayer().isSneaking() || !event.getPlayer().hasPermission("treasurechests.setup")) {
            event.setCancelled(true);
            return;
        }

        if ( event.getBlock().getLocation().equals(chest.location()) ) {
            manager.delete(chest);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !manager.isTreasureChestType(block.getType())) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("treasurechests.setup") || !player.isSneaking()) {
            return;
        }

        ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
        if (hand != null && hand.getType() != Material.AIR) {
            return;
        }

        event.setCancelled(true);

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
                .withTitle(ChatColor.DARK_PURPLE + "Create Treasure Chest")
                .withItem(
                        ItemStackBuilder.of(Material.NETHER_STAR).withName(ChatColor.YELLOW + "Create Treasure Chest").build(),
                        (event) -> {
                            manager.addChest(block.getLocation()).thenAccept(chest -> {
                                player.sendMessage(ChatColor.GREEN + "Treasure chest created.");
                                info(player, chest);
                            });
                            return true;
                        })
                .build().open(player);
    }

    private void info(Player player, BTreasureChest chest) {
        ItemStack inspect = ItemStackBuilder.of(Material.GOLD_INGOT)
                .withName(ChatColor.GOLD + "Inspect loot")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to delete."
                ).build();

        ItemStack duration = ItemStackBuilder.of(Material.CLOCK)
                .withName(ChatColor.BLUE + "Respawn Duration")
                .withLore(
                        ChatColor.GRAY + "Duration: " + ChatColor.GOLD + format(chest.respawnTime()),
                        "",
                        ChatColor.GRAY + "Left click to change."
                ).build();

        ItemStack mode = ItemStackBuilder.of(Material.FLOWER_POT)
                .withName(ChatColor.YELLOW + "Chest Mode")
                .withLore(
                        ChatColor.GRAY + "Mode: " + ChatColor.GOLD + chest.mode().name(),
                        "",
                        ChatColor.GRAY + "Left click to cycle."
                ).build();

        ItemStack delete = ItemStackBuilder.of(Material.LAVA_BUCKET)
                .withName(ChatColor.RED + "Delete chest")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to delete.")
                .build();

        BukkitMasterGUI.builder()
                .withTitle(ChatColor.DARK_PURPLE + "Treasure Chest Info")
                .withItem(inspect, (event) -> {
                    loot(player, chest);
                    return true;
                })
                .withItem(duration, (event) -> {
                    duration(player, chest);
                    return true;
                })
                .withItem(mode, (event) -> {
                    int ordinal = chest.mode().ordinal() + 1;
                    if ( ordinal >= ChestMode.values().length ) {
                        ordinal = 0;
                    }

                    chest.setChestMode(ChestMode.values()[ordinal]);
                    manager.save(chest);
                    info(player, chest);
                    return true;
                })
                .withItem(delete, (event) -> {
                    delete(player, chest);
                    return true;
                })
                .build()
                .open(player);
    }

    private void delete(Player player, BTreasureChest chest) {
        ItemStack confirm = ItemStackBuilder.of(Material.LIME_TERRACOTTA).withName(ChatColor.GREEN + "Confirm").build();
        ItemStack cancel = ItemStackBuilder.of(Material.RED_TERRACOTTA).withName(ChatColor.RED + "Cancel").build();

        BukkitMasterGUI.builder()
                .withTitle(ChatColor.DARK_PURPLE + "Delete Treasure Chest")
                .withItem(confirm, (event) -> {
                    manager.delete(chest);
                    player.sendMessage(ChatColor.GREEN + "Treasure chest deleted.");
                    player.closeInventory();
                    return true;
                })
                .withItem(cancel, (event) -> {
                    info(player, chest);
                    return true;
                })
                .build().open(player);
    }

    private final ItemStack back = ItemStackBuilder.of(Material.PAPER).withName(ChatColor.GREEN + "Back").build();

    private void loot(Player player, BTreasureChest chest) {
        IBukkitMenu menu = BukkitMasterGUI.create(54, ChatColor.DARK_PURPLE + "Drop items to add loot.");
        for (int i = 0; i < chest.loot().size(); i++) {
            BTreasureLoot loot = chest.loot().get(i);
            ItemStack item = ItemStackBuilder.of(loot.item())
                    .withLore(
                            ChatColor.GRAY + "Chance: " + ChatColor.GOLD + loot.chance() + "%",
                            "",
                            ChatColor.GRAY + "Left click to change.",
                            ChatColor.GRAY + "Right click to delete."
                    ).build();

            menu.setItem(i, item, (event) -> {
                if (event.getClick() == ClickType.RIGHT) {
                    manager.delete(loot);
                    loot(player, chest);
                    return true;
                } else if ( event.getClick() == ClickType.LEFT) {
                    amount(player, loot);
                    return true;
                }
                return false;
            });
        }

        // shift click add items
        menu.addClickListener(event -> {
            if (event.getClickedInventory() == event.getInventory()) {
                return;
            }
            if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT
                    || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            manager.save(chest.addLoot(event.getCurrentItem().clone()));
            loot(player, chest);
        });

        // drag and drop add item
        menu.addClickListener(event -> {
            if (event.getClickedInventory() != event.getInventory() || event.getClick() != ClickType.LEFT
                    || event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                return;
            }
            manager.save(chest.addLoot(event.getCursor().clone()));
            loot(player, chest);
        });

        // back item
        menu.setItem(49, back, (event) -> {
            info(player, chest);
            return true;
        });

        menu.open(player);
    }

    private final ItemStack min1 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQyNDU0ZTRjNjdiMzIzZDViZTk1M2I1YjNkNTQxNzRhYTI3MTQ2MDM3NGVlMjg0MTBjNWFlYWUyYzExZjUifX19").build();
    private final ItemStack min5 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzZjU2NWE4ODkyOGVlNWE5ZDY4NDNkOTgyZDc4ZWFlNmI0MWQ5MDc3ZjJhMWU1MjZhZjg2N2Q3OGZiIn19fQ==").build();
    private final ItemStack min10 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5ODdmNDNmZjU3ZDRkYWJhYTJkMmNlYjlmMDFmYzZlZTQ2ZGIxNjJhNWUxMmRmZGJiNTdmZDQ2ODEzMmI4In19fQ==").build();
    private final ItemStack minH = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ5ZTMzZDc0M2VlMzQyMjQzMzEyMjkxY2NkY2ZmZDdmY2NhNWJkYzhhNmE4NDU5ZWU4ZTYyY2U1N2FkZDcifX19").build();

    private final ItemStack plus1 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM0N2I3YTVhNmJiNjQxOWQzM2ViNDYxMDI2NTBjYzkxYmZkYzE1OWQ4ZmYxZjU1OTk2ZDRmMGFkNWJjNzU4In19fQ==").build();
    private final ItemStack plus5 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE2NzE0NGIzMjNhZDRkY2ViZmQxYzZhNjMwNzQzYzcwMmM0ZGE0NTUyMjg5YTkyNDIxZWFmYzRjMmQ0YjQzIn19fQ==").build();
    private final ItemStack plus10 = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjFmODNkNmMxN2U0ZWZlODRlZGJhMzkxNzAzNzNkZjQ3NDgzNTRlYzNiMzM0MmU2YWEwYjRlZWE0YjYyMTkyIn19fQ==").build();
    private final ItemStack plusH = ItemStackBuilder.skull().withSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDgyZDljZjg1NzdlYjBhM2QwMzFlZWMzNzBlODc5NjI5YjlhZjVhMmZjMjkyYjUyZDVmMmJmNzRiZjk0ZmY0MiJ9fX0=").build();

    private void duration(Player player, BTreasureChest chest) {
        IBukkitMenu menu = BukkitMasterGUI.create(45, ChatColor.DARK_PURPLE + "Duration: " +
                ChatColor.LIGHT_PURPLE + format(chest.respawnTime()));

        Function<Integer, Function<InventoryClickEvent, Boolean>> callback = (time) -> (event) -> {
            chest.setRespawnTime(chest.respawnTime() + time);
            manager.save(chest);
            duration(player, chest);
            return true;
        };

        menu.setItem(12, ItemStackBuilder.of(min1).withName(ChatColor.GREEN + "-1 Minute").build(), callback.apply(-60));
        menu.setItem(13, ItemStackBuilder.of(min10).withName(ChatColor.GREEN + "-10 Minutes").build(), callback.apply(-600));
        menu.setItem(14, ItemStackBuilder.of(minH).withName(ChatColor.GREEN + "-1 Hour").build(), callback.apply(-3600));

        menu.setItem(21, ItemStackBuilder.of(plus1).withName(ChatColor.GREEN + "+1 Minute").build(), callback.apply(60));
        menu.setItem(22, ItemStackBuilder.of(plus10).withName(ChatColor.GREEN + "+10 Minutes").build(), callback.apply(600));
        menu.setItem(23, ItemStackBuilder.of(plusH).withName(ChatColor.GREEN + "+1 Hour").build(), callback.apply(3600));

        menu.setItem(40, back, (event) -> {
            info(player, chest);
            return true;
        });

        menu.open(player);
    }

    private void amount(Player player, BTreasureLoot loot) {
        IBukkitMenu menu = BukkitMasterGUI.create(45, ChatColor.DARK_PURPLE + "Chance: " +
                ChatColor.LIGHT_PURPLE + loot.chance() + "%");

        Function<Integer, Function<InventoryClickEvent, Boolean>> callback = (amount) -> (event) -> {
            int chance = Math.min(100, Math.max(0, loot.chance() + amount));
            loot.setChance(chance);
            manager.save(loot);
            amount(player, loot);
            return true;
        };

        menu.setItem(12, ItemStackBuilder.of(min1).withName(ChatColor.GREEN + "-1%").build(), callback.apply(-1));
        menu.setItem(13, ItemStackBuilder.of(min5).withName(ChatColor.GREEN + "-5%").build(), callback.apply(-5));
        menu.setItem(14, ItemStackBuilder.of(min10).withName(ChatColor.GREEN + "-10%").build(), callback.apply(-10));

        menu.setItem(21, ItemStackBuilder.of(plus1).withName(ChatColor.GREEN + "+1%").build(), callback.apply(1));
        menu.setItem(22, ItemStackBuilder.of(plus5).withName(ChatColor.GREEN + "+5%").build(), callback.apply(5));
        menu.setItem(23, ItemStackBuilder.of(plus10).withName(ChatColor.GREEN + "+10%").build(), callback.apply(10));

        menu.setItem(40, back, (event) -> {
            info(player, loot.chest);
            return true;
        });

        menu.open(player);
    }

    private String format(int seconds) {
        String str = "";
        int hours = seconds / 3600;
        if (hours > 0) {
            str = hours + "h ";
            seconds -= hours * 3600;
        }

        int minutes = seconds / 60;
        if (minutes > 0) {
            str += minutes + "m ";
            seconds -= minutes * 60;
        }

        if (seconds > 0 || str.length() == 0) {
            str += seconds + "s";
        }
        return str;
    }

}
