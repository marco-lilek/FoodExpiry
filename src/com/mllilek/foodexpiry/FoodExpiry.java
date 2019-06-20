package com.mllilek.foodexpiry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FoodExpiry extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MealListener(), this);
        getServer().getPluginManager().registerEvents(new PickupFoodListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
    }

    private static final String PLUGIN_NAME = "foodexpiry";

    private static final String EXPIRY_PREFIX = "expires ";
    private static final SimpleDateFormat EXPIRY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int TICKS_IN_DAY = 24000;
    private static final int TICKS_IN_SECOND = 20;
    private static final int TICKS_IN_MINUTE = 20;

    private final Random random = new Random();

    private static boolean isSoup(Material mat) {
        return mat.equals(Material.BEETROOT_SOUP) ||
                mat.equals(Material.MUSHROOM_STEW) ||
                mat.equals(Material.RABBIT_STEW);
    }

    private final class MealListener implements Listener {

        @org.bukkit.event.EventHandler(priority = EventPriority.NORMAL)
        public void onConsumeItem(org.bukkit.event.player.PlayerItemConsumeEvent e) {
            Player ply = e.getPlayer();

            ItemStack foodStack = ply.getInventory().getItemInMainHand();
            Material material = foodStack.getType();
            if (!material.isEdible()) {
                return;
            }

            ItemMeta meta = foodStack.getItemMeta();
            if (meta == null) {
                return;
            }

            if (!meta.hasLore()) {
                return;
            }

            String foodExpiryInLore = meta.getLore().stream()
                    .filter(x->x.startsWith(EXPIRY_PREFIX)).findFirst().orElse(null);
            if (foodExpiryInLore == null) {
                return;
            }

            String foodExpiry = foodExpiryInLore.substring(EXPIRY_PREFIX.length());
            try {
                Instant foodExpiryAsTime = EXPIRY_FORMAT.parse(foodExpiry).toInstant();
                Instant now = getCurrentWorldTime(ply, 0);
                if (now.compareTo(foodExpiryAsTime) >= 0) {
                    // Remove the item from the players inventory
                    if (isSoup(foodStack.getType())) {
                        ply.getInventory().setItemInMainHand(
                                new ItemStack(Material.BOWL, 1));
                    } else {
                        if (foodStack.getAmount() <= 1) {
                            ply.getInventory().removeItem(ply.getInventory().getItemInMainHand());
                        } else {
                            foodStack.setAmount(foodStack.getAmount() - 1);
                            ply.getInventory().setItemInMainHand(foodStack);
                        }
                    }

                    // Apply status effects

                    // player is unlucky
                    // they got sick
                    if (random.nextInt(4) == 0) {
                        ply.sendMessage(ChatColor.RED + "the expired food made you sick!" + ChatColor.RESET);
                        ply.setSaturation(0);
                        int effectDurationSeconds = random.nextInt(30);
                        if (random.nextBoolean()) {
                            // Make them confused
                            ply.addPotionEffect(new PotionEffect(
                                    PotionEffectType.CONFUSION,effectDurationSeconds * TICKS_IN_SECOND,
                                    random.nextInt(2) /* amplifier */));
                        } else {
                            // Or make them sick
                            ply.addPotionEffect(new PotionEffect(
                                    PotionEffectType.POISON,effectDurationSeconds * TICKS_IN_SECOND,
                                    random.nextInt(2) /* amplifier */));
                        }

                        // Always make them hungry
                        int hungerDurationInSec = random.nextInt(3 * 60);
                        ply.addPotionEffect(new PotionEffect(
                                PotionEffectType.POISON,hungerDurationInSec * TICKS_IN_SECOND,
                                1 /* amplifier */));

                        e.setCancelled(true);
                    } else {
                        ply.sendMessage(ChatColor.YELLOW + "careful, eating expired food can make you sick!" + ChatColor.RESET);
                    }
                }
            } catch (ParseException exp) {
                return;
            }
        }
    }

    private static Instant getCurrentWorldTime(Player ply, int daysOffset) {
        long fullTimeInTicks = ply.getWorld().getFullTime();
        long daysSinceEpoch = (fullTimeInTicks / TICKS_IN_DAY) + daysOffset;
        return Instant.ofEpochSecond(TimeUnit.DAYS.toSeconds(daysSinceEpoch));
    }

    private static String generateExpiry(Player player, int expiryOffset) {
        Instant expiryDate = getCurrentWorldTime(player, expiryOffset);
        return EXPIRY_PREFIX + formatAsExpiryDate(expiryDate);
    }

    private static String formatAsExpiryDate(Instant time) {
        return EXPIRY_FORMAT.format(Date.from(time));
    }

    private static boolean lastsForever(Material mat) {
        return mat.equals(Material.GOLDEN_APPLE) ||
                mat.equals(Material.ENCHANTED_GOLDEN_APPLE) ||
                mat.equals(Material.GOLDEN_CARROT);
    }

    private final class PickupFoodListener implements Listener {
        private void addExpiryToFood(ItemStack itemStack, Player player) {
            if (itemStack == null) {
                return;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }

            Material material = itemStack.getType();
            if (!material.isEdible() || lastsForever(material)) {
                return;
            }

            String expiry = generateExpiry(player, getLongevityForMaterial(material));

            if (!itemMeta.hasLore()) {
                itemMeta.setLore(Collections.singletonList(expiry));
            } else {
                List<String> itemLore = itemMeta.getLore();

                boolean hasExpiry = false;
                for (int i = 0; i < itemLore.size(); i++) {
                    String lore = itemLore.get(i);
                    if (lore.startsWith(EXPIRY_PREFIX)) {
                        itemLore.set(i, expiry);
                        hasExpiry = true;
                    }
                }

                if (!hasExpiry) {
                    itemLore.add(expiry);
                }

                itemMeta.setLore(itemLore);
            }

            itemStack.setItemMeta(itemMeta);
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onFoodPickupEvent(EntityPickupItemEvent e) {
            Player player = (Player)e.getEntity();

            if (!e.getEntityType().equals(EntityType.PLAYER)) {
                return;
            }

            Item item = e.getItem();
            addExpiryToFood(item.getItemStack(), player);
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onGetPreparedFoodEvent(InventoryClickEvent e) {
            List<HumanEntity> viewers = e.getViewers();
            if (viewers.size() != 1) {
                return;
            }

            Player player = (Player)e.getViewers().get(0);
            ItemStack currentItem = e.getCurrentItem();
            if (currentItem == null) {
                return;
            }

            InventoryType source = e.getView().getTopInventory().getType();

            if (!(source.equals(InventoryType.FURNACE)
                    || source.equals(InventoryType.WORKBENCH))) {
                return;
            }

            addExpiryToFood(currentItem, player);
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onRightClickClockEvent(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Action action = event.getAction();
            ItemStack item = event.getItem();

             if (!(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))) {
                 return;
             }
             if (item == null || !item.getType().equals(Material.CLOCK)) {
                 return;
             }

             player.sendMessage("current date " +
                     formatAsExpiryDate(getCurrentWorldTime(player, 0)));
        }
    }

    private int getLongevityForMaterial(Material material) {
        switch (material) {
            case BREAD:
                return 7;
            case MUSHROOM_STEW:
            case RABBIT_STEW:
            case BEETROOT_SOUP:
                return 10;
            case TROPICAL_FISH:
                return 5;
            default:
                return 3;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase(PLUGIN_NAME)) {
            return true;
        }

        if (args.length != 0) {
            return false;
        }

        if (!(sender instanceof Player)) {
            return false;
        }

        Player ply = (Player) sender;
        List<String> msg = new ArrayList<>();
        msg.add("# === Food Expiry ===");
        msg.add("# Avoid eating expired foods, they might make you sick.");
        msg.add("# ");
        msg.add("# Bread, fish, and soups last long");
        msg.add("# ");
        msg.add("# Right click with a clock to get the current world date");
        msg.add("# ===================");

        msg.forEach(ply::sendMessage);
        return true;
    }

}