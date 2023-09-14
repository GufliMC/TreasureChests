package com.guflimc.treasurechests.spigot.listeners;

import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.brick.gui.spigot.api.ISpigotMenu;
import com.guflimc.brick.gui.spigot.item.ItemStackBuilder;
import com.guflimc.brick.gui.spigot.menu.SpigotMenu;
import com.guflimc.brick.gui.spigot.menu.SpigotMenuItem;
import com.guflimc.treasurechests.spigot.TreasureChestManager;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureChest;
import com.guflimc.treasurechests.spigot.data.beans.BTreasureLoot;
import com.guflimc.treasurechests.spigot.data.beans.ChestMode;
import com.guflimc.treasurechests.spigot.data.beans.ParticleEffect;
import com.guflimc.treasurechests.spigot.particle.ParticleJobManager;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlayerChestSetupListener implements Listener {

    private final TreasureChestManager manager;
    private final ParticleJobManager particleJobManager;

    private final Map<Player, BTreasureChest> changeTitleSession = new HashMap<>();
    private final Map<Player, BTreasureChest> clipboard = new HashMap<>();

    public PlayerChestSetupListener(TreasureChestManager manager, ParticleJobManager particleJobManager) {
        this.manager = manager;
        this.particleJobManager = particleJobManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!manager.isTreasureChestType(event.getBlock().getType())) {
            return;
        }

        BTreasureChest chest = manager.chestAt(event.getBlock());
        if (chest == null) {
            return;
        }

        if (!event.getPlayer().isSneaking() || !event.getPlayer().hasPermission("treasurechests.setup")) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getLocation().equals(chest.location())) {
            particleJobManager.stop(chest);
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        changeTitleSession.remove(event.getPlayer());
        clipboard.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        BTreasureChest chest = changeTitleSession.get(player);
        if (chest == null) {
            return;
        }

        event.setCancelled(true);
        changeTitleSession.remove(player);

        Bukkit.getScheduler().runTask(manager.plugin, () -> {
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                info(player, chest);
                return;
            }

            chest.setTitle(MiniMessage.miniMessage().deserialize(event.getMessage()));
            manager.save(chest);
            info(player, chest);
        });
    }

    // MENUS

    private void create(Player player, Block block) {
        SpigotBrickGUI.builder()
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
        ISpigotMenu menu = SpigotBrickGUI.create(54, ChatColor.DARK_PURPLE + "Treasure Chest Info");

        // INSPECT
        ItemStack inspect = ItemStackBuilder.of(Material.GOLD_INGOT)
                .withName(ChatColor.YELLOW + "Inspect loot")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to delete."
                ).build();
        menu.setItem(11, inspect, (event) -> {
            loot(player, chest);
            return true;
        });

        // DURATION
        ItemStack duration = ItemStackBuilder.of(Material.CLOCK)
                .withName(ChatColor.YELLOW + "Respawn Duration")
                .withLore(
                        ChatColor.GRAY + "Duration: " +
                                (chest.respawnTime() == 0 ? ChatColor.RED + "DISABLED" :
                                        ChatColor.GOLD + format(chest.respawnTime())),
                        "",
                        ChatColor.GRAY + "Left click to change.",
                        ChatColor.GRAY + "Right click to disable."
                ).build();
        menu.setItem(13, duration, (event) -> {
            if (event.isRightClick()) {
                chest.setRespawnTime(0);
                manager.save(chest);
                info(player, chest);
                return true;
            }

            duration(player, chest);
            return true;
        });

        // MODE
        ItemStack mode = ItemStackBuilder.of(Material.FLOWER_POT)
                .withName(ChatColor.YELLOW + "Chest Mode")
                .withLore(
                        ChatColor.GRAY + "Mode: " + ChatColor.GOLD + chest.mode().name(),
                        "",
                        ChatColor.GRAY + "Left click to cycle."
                ).build();
        menu.setItem(15, mode, (event) -> {
            int ordinal = chest.mode().ordinal() + 1;
            if (ordinal >= ChestMode.values().length) {
                ordinal = 0;
            }

            chest.setChestMode(ChestMode.values()[ordinal]);
            manager.save(chest);
            info(player, chest);
            return true;
        });

        // SPLIT STACKS
        ItemStack splitStacks = ItemStackBuilder.of(Material.HOPPER)
                .withName(ChatColor.YELLOW + "Split Stacks")
                .withLore(
                        ChatColor.GRAY + "Value: " + (chest.splitStacks() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"),
                        "",
                        ChatColor.GRAY + "Left click to toggle."
                ).build();
        menu.setItem(20, splitStacks, (event) -> {
            chest.setSplitStacks(!chest.splitStacks());
            manager.save(chest);
            info(player, chest);
            return true;
        });

        // TITLE
        ItemStack title = ItemStackBuilder.of(Material.NAME_TAG)
                .withName(ChatColor.YELLOW + "Title")
                .withLore(
                        ChatColor.GRAY + "Value: " + BukkitComponentSerializer.legacy().serialize(chest.title()),
                        "",
                        ChatColor.GRAY + "Left click to change."
                ).build();
        menu.setItem(22, title, (event) -> {
            changeTitleSession.put(player, chest);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Enter a new title in the chat or type 'cancel' to abort.");
            return true;
        });

        // PARTICLES
        ItemStack particles = ItemStackBuilder.of(Material.BLAZE_POWDER)
                .withName(ChatColor.YELLOW + "Particle effects")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to change particle effects.")
                .build();
        menu.setItem(24, particles, (event) -> {
            particles(player, chest);
            return true;
        });

        // COPY
        ItemStack copy = ItemStackBuilder.of(Material.ENDER_PEARL)
                .withName(ChatColor.GREEN + "Copy chest settings")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to copy.")
                .build();
        menu.setItem(38, copy, (event) -> {
            clipboard.put(player, chest);
            return true;
        });

        // PASTE
        if (clipboard.containsKey(player) && !clipboard.get(player).equals(chest)) {
            ItemStack paste = ItemStackBuilder.of(Material.SLIME_BALL)
                    .withName(ChatColor.GREEN + "Paste chest settings")
                    .withLore(
                            "",
                            ChatColor.GRAY + "Left click to paste.")
                    .build();
            menu.setItem(39, paste, (event) -> {
                paste(player, chest);
                return true;
            });
        }

//        // FORCE REFILL
//        ItemStack refill = ItemStackBuilder.of(Material.COOKIE)
//                .withName(ChatColor.GOLD + "Force Refill")
//                .withLore(
//                        "",
//                        ChatColor.GRAY + "Left click to refill all inventories.")
//                .build();
//        menu.setItem(40, refill, (event) -> {
//
//            return true;
//        });
        // TODO

        // DELETE
        ItemStack delete = ItemStackBuilder.of(Material.LAVA_BUCKET)
                .withName(ChatColor.RED + "Delete chest")
                .withLore(
                        "",
                        ChatColor.GRAY + "Left click to delete.")
                .build();
        menu.setItem(42, delete, (event) -> {
            delete(player, chest);
            return true;
        });

        menu.open(player);
    }

    private void delete(Player player, BTreasureChest chest) {
        ItemStack confirm = ItemStackBuilder.of(Material.LIME_TERRACOTTA).withName(ChatColor.GREEN + "Confirm").build();
        ItemStack cancel = ItemStackBuilder.of(Material.RED_TERRACOTTA).withName(ChatColor.RED + "Cancel").build();

        SpigotBrickGUI.builder()
                .withTitle(ChatColor.DARK_PURPLE + "Delete treasure chest")
                .withItem(confirm, (event) -> {
                    particleJobManager.stop(chest);
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

    private void paste(Player player, BTreasureChest chest) {
        ItemStack confirm = ItemStackBuilder.of(Material.LIME_TERRACOTTA).withName(ChatColor.GREEN + "Confirm")
                .withLore("", ChatColor.GRAY + "This will override all current settings.").build();
        ItemStack cancel = ItemStackBuilder.of(Material.RED_TERRACOTTA).withName(ChatColor.RED + "Cancel").build();

        SpigotBrickGUI.builder()
                .withTitle(ChatColor.DARK_PURPLE + "Paste treasure chest")
                .withItem(confirm, (event) -> {
                    // copy all values
                    BTreasureChest clip = clipboard.get(player);
                    chest.setChestMode(clip.mode());
                    chest.setRespawnTime(clip.respawnTime());
                    chest.setSplitStacks(clip.splitStacks());
                    chest.setTitle(clip.title());
                    chest.setParticleEffect(clip.particleEffect());
                    chest.loot().forEach(chest::removeLoot);
                    clip.loot().forEach(l -> {
                        BTreasureLoot loot = chest.addLoot(l.item());
                        loot.setChance(l.chance());
                    });
                    manager.save(chest);

                    info(player, chest);
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
        int size = manager.isDoubleChest(chest.location()) ? 54 : 36;

        // remove excess loot
        if (chest.loot().size() > size) {
            manager.delete(chest.loot().subList(size, chest.loot().size()).toArray(BTreasureLoot[]::new));
            manager.save(chest);
        }

        ISpigotMenu menu = SpigotBrickGUI.create(size, ChatColor.DARK_PURPLE + "Drop items to add loot.");
        for (int i = 0; i < Math.min(size - 9, chest.loot().size()); i++) {
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
                } else if (event.getClick() == ClickType.LEFT) {
                    dropchance(player, loot);
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
        menu.setItem(size - 5, back, (event) -> {
            info(player, chest);
            return true;
        });

        menu.open(player);
    }

    private final ItemStack min1 = ItemStackBuilder.skull().withTexture("8d2454e4c67b323d5be953b5b3d54174aa271460374ee28410c5aeae2c11f5").build();
    private final ItemStack min5 = ItemStackBuilder.skull().withTexture("df3f565a88928ee5a9d6843d982d78eae6b41d9077f2a1e526af867d78fb").build();
    private final ItemStack min10 = ItemStackBuilder.skull().withTexture("f5987f43ff57d4dabaa2d2ceb9f01fc6ee46db162a5e12dfdbb57fd468132b8").build();
    private final ItemStack minHour = ItemStackBuilder.skull().withTexture("4d9e33d743ee342243312291ccdcffd7fcca5bdc8a6a8459ee8e62ce57add7").build();
    private final ItemStack minDay = ItemStackBuilder.skull().withTexture("ed68ae3150d81e4c0a9d172bd84c4ff73cdc0b87fee8ec661213468d544483").build();
    private final ItemStack minMonth = ItemStackBuilder.skull().withTexture("116afb385724783947a6052ca2758f9c937cb8130c0eac1a3f068dc55495393").build();

    private final ItemStack plus1 = ItemStackBuilder.skull().withTexture("7347b7a5a6bb6419d33eb46102650cc91bfdc159d8ff1f55996d4f0ad5bc758").build();
    private final ItemStack plus5 = ItemStackBuilder.skull().withTexture("2a67144b323ad4dcebfd1c6a630743c702c4da4552289a92421eafc4c2d4b43").build();
    private final ItemStack plus10 = ItemStackBuilder.skull().withTexture("f1f83d6c17e4efe84edba39170373df4748354ec3b3342e6aa0b4eea4b62192").build();
    private final ItemStack plusHour = ItemStackBuilder.skull().withTexture("d82d9cf8577eb0a3d031eec370e879629b9af5a2fc292b52d5f2bf74bf94ff42").build();
    private final ItemStack plusDay = ItemStackBuilder.skull().withTexture("8081c171fb4675f9cbb6cfbc297947f14049a6588d2870796d90d5b02c0").build();
    private final ItemStack plusMonth = ItemStackBuilder.skull().withTexture("4c1713d90ca5998716eaa7dd302f6c8d672595dcc6956c2e20e94184bae2").build();

    private void duration(Player player, BTreasureChest chest) {
        ISpigotMenu menu = SpigotBrickGUI.create(45, ChatColor.DARK_PURPLE + "Duration: " +
                ChatColor.LIGHT_PURPLE + format(chest.respawnTime()));

        Function<Integer, Function<InventoryClickEvent, Boolean>> callback = (time) -> (event) -> {
            chest.setRespawnTime(chest.respawnTime() + time);
            manager.save(chest);
            duration(player, chest);
            return true;
        };

        menu.setItem(11, ItemStackBuilder.of(min1).withName(ChatColor.GREEN + "-1 Minute").build(), callback.apply(-60));
        menu.setItem(12, ItemStackBuilder.of(min10).withName(ChatColor.GREEN + "-10 Minutes").build(), callback.apply(-600));
        menu.setItem(13, ItemStackBuilder.of(minHour).withName(ChatColor.GREEN + "-1 Hour").build(), callback.apply(-3600));
        menu.setItem(14, ItemStackBuilder.of(minDay).withName(ChatColor.GREEN + "-1 Day").build(), callback.apply(-86400));
        menu.setItem(15, ItemStackBuilder.of(minMonth).withName(ChatColor.GREEN + "-1 Month").build(), callback.apply(-2592000));

        menu.setItem(20, ItemStackBuilder.of(plus1).withName(ChatColor.GREEN + "+1 Minute").build(), callback.apply(60));
        menu.setItem(21, ItemStackBuilder.of(plus10).withName(ChatColor.GREEN + "+10 Minutes").build(), callback.apply(600));
        menu.setItem(22, ItemStackBuilder.of(plusHour).withName(ChatColor.GREEN + "+1 Hour").build(), callback.apply(3600));
        menu.setItem(23, ItemStackBuilder.of(plusDay).withName(ChatColor.GREEN + "+1 Day").build(), callback.apply(86400));
        menu.setItem(24, ItemStackBuilder.of(plusMonth).withName(ChatColor.GREEN + "+1 Month").build(), callback.apply(2592000));

        menu.setItem(40, back, (event) -> {
            info(player, chest);
            return true;
        });

        menu.open(player);
    }

    private void dropchance(Player player, BTreasureLoot loot) {
        ISpigotMenu menu = SpigotBrickGUI.create(45, ChatColor.DARK_PURPLE + "Chance: " +
                ChatColor.LIGHT_PURPLE + loot.chance() + "%");

        Function<Integer, Function<InventoryClickEvent, Boolean>> callback = (amount) -> (event) -> {
            int chance = Math.min(100, Math.max(0, loot.chance() + amount));
            loot.setChance(chance);
            manager.save(loot);
            dropchance(player, loot);
            return true;
        };

        menu.setItem(12, ItemStackBuilder.of(min1).withName(ChatColor.GREEN + "-1%").build(), callback.apply(-1));
        menu.setItem(13, ItemStackBuilder.of(min5).withName(ChatColor.GREEN + "-5%").build(), callback.apply(-5));
        menu.setItem(14, ItemStackBuilder.of(min10).withName(ChatColor.GREEN + "-10%").build(), callback.apply(-10));

        menu.setItem(21, ItemStackBuilder.of(plus1).withName(ChatColor.GREEN + "+1%").build(), callback.apply(1));
        menu.setItem(22, ItemStackBuilder.of(plus5).withName(ChatColor.GREEN + "+5%").build(), callback.apply(5));
        menu.setItem(23, ItemStackBuilder.of(plus10).withName(ChatColor.GREEN + "+10%").build(), callback.apply(10));

        menu.setItem(40, back, (event) -> {
            loot(player, loot.chest);
            return true;
        });

        menu.open(player);
    }

    private String format(int seconds) {
        String str = "";
        int months = seconds / 2592000;
        if (months > 0) {
            str += months + "M ";
            seconds -= months * 2592000;
        }

        int days = seconds / 86400;
        if (days > 0) {
            str += days + "D ";
            seconds -= days * 86400;
        }

        int hours = seconds / 3600;
        if (hours > 0) {
            str += hours + "h ";
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

    private void particles(Player player, BTreasureChest chest) {
        SpigotBrickGUI.paginatedBuilder().withTitle(ChatColor.DARK_PURPLE + "Particles")
                .withHotbarItem(4, back, (event) -> {
                    info(player, chest);
                    return true;
                })
                .withItems(ParticleEffect.ParticleType.values().length, index -> {
                    ParticleEffect.ParticleType type = ParticleEffect.ParticleType.values()[index];
                    ItemStackBuilder b = ItemStackBuilder.of(type.material());

                    if (chest.particleEffect() != null && chest.particleEffect().type() == type) {
                        b
                                .withName(ChatColor.GREEN + type.display() + ChatColor.GRAY + " (Selected)")
                                .withEnchantment(Enchantment.SILK_TOUCH, 1)
                                .withItemFlag(ItemFlag.HIDE_ENCHANTS)
                                .build();
                    } else {
                        b.withName(ChatColor.YELLOW + type.display());
                    }

                    return new SpigotMenuItem(b.build(), SpigotMenu.soundWrapper((event) -> {
                        ParticleEffect effect;
                        if (chest.particleEffect() != null) {
                            effect = new ParticleEffect(type, chest.particleEffect().pattern());
                        } else {
                            effect = new ParticleEffect(type, ParticleEffect.ParticlePattern.RANDOM);
                        }
                        chest.setParticleEffect(effect);
                        particleJobManager.start(chest);
                        manager.save(chest);
                        particles(player, chest);
                        return true;
                    }));
                })
                .build()
                .open(player);
    }

}
