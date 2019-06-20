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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private static final SimpleDateFormat EXPIRY_FORMAT = new SimpleDateFormat("yy-MM-dd");
    private static final int EXPIRY_DAYS_OFFSET = 3;
    private static final int TICKS_IN_DAY = 24000;

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

            String foodExpiry = foodExpiryInLore.substring(EXPIRY_PREFIX.length(), foodExpiryInLore.length());
            try {
                Instant foodExpiryAsTime = EXPIRY_FORMAT.parse(foodExpiry).toInstant();
                Instant now = getCurrentWorldTime(ply);
                if (now.compareTo(foodExpiryAsTime) < 0) {
                } else {
                    ply.sendMessage(ChatColor.RED + "eating expired food" + ChatColor.RESET);
                    if (foodStack.getAmount() <= 1) {
                        ply.getInventory().removeItem(ply.getInventory().getItemInMainHand());
                    } else {
                        foodStack.setAmount(foodStack.getAmount() - 1);
                        ply.getInventory().setItemInMainHand(foodStack);
                    }
                    e.setCancelled(true);
                }
            } catch (ParseException exp) {
                return;
            }
        }
    }

    private static Instant getCurrentWorldTime(Player ply) {
        long fullTimeInTicks = ply.getWorld().getFullTime();
        long daysSinceEpoch = (fullTimeInTicks / TICKS_IN_DAY);
        return Instant.ofEpochSecond(TimeUnit.DAYS.toSeconds(daysSinceEpoch));
    }

    private static String generateExpiry(Player player) {
        Instant expiryDate = getCurrentWorldTime(player).plus(EXPIRY_DAYS_OFFSET, ChronoUnit.DAYS);
        return EXPIRY_PREFIX + formatAsExpiryDate(expiryDate);
    }

    private static String formatAsExpiryDate(Instant time) {
        return EXPIRY_FORMAT.format(Date.from(time));
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

            if (!itemStack.getType().isEdible()) {
                return;
            }

            String expiry = generateExpiry(player);

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
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sethunger")) {
            if (args.length != 0) {
                return false;
            }

            if (!(sender instanceof Player)) {
                return false;
            }

            Player ply = (Player) sender;
            ply.setFoodLevel(1);
            return true;
        }
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
        ply.sendMessage("current date " + formatAsExpiryDate(getCurrentWorldTime(ply)));
        return true;
    }

}