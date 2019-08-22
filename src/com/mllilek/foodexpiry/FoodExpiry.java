package com.mllilek.foodexpiry;

import com.mllilek.foodexpiry.expiry.FoodExpiryManager;
import com.mllilek.foodexpiry.sickness.SicknessManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class FoodExpiry extends JavaPlugin {

    private FoodExpiryManager foodExpiryManager;
    private SicknessManager sicknessManager;
    private CommandProcessor commandProcessor;
    private boolean showFoodTypeWhenEaten;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EatFoodListener(), this);
        getServer().getPluginManager().registerEvents(new PickupFoodListener(), this);

        getConfig().options().copyDefaults(true);
        saveConfig();

        Configuration config = getConfig();
        Random random = new Random();
        sicknessManager = SicknessManager.fromConfig(
                random, config.getConfigurationSection("sickness"));
        foodExpiryManager = FoodExpiryManager.fromConfig(
                config.getConfigurationSection("expiry"));
        commandProcessor = new CommandProcessor();
        showFoodTypeWhenEaten = config.getBoolean("showFoodTypeWhenEaten");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
    }

    private final class EatFoodListener implements Listener {
        @org.bukkit.event.EventHandler(priority = EventPriority.NORMAL)
        public void onEatFoodEvent(PlayerItemConsumeEvent e) {
            Player ply = e.getPlayer();
            ItemStack foodStack = ply.getInventory().getItemInMainHand();

            if (showFoodTypeWhenEaten) {
                ply.sendMessage("the food type is " + foodStack.getType().toString());
            }

            if (foodExpiryManager.isExpired(foodStack, ply.getWorld())) {
                if (sicknessManager.maybeMakePlayerSick(ply)) {
                    InventoryHelper.removeItemFromMainHand(ply);
                    e.setCancelled(true);
                    ply.sendMessage(
                            ChatColor.RED +
                            "the expired food made you sick!" +
                            ChatColor.RESET);
                } else {
                    ply.sendMessage(
                            ChatColor.YELLOW +
                            "careful, eating expired food can make you sick!" +
                            ChatColor.RESET);
                }
            }
        }
    }

    private class AddExpiryToAllItemsInInventory implements Runnable {
        private Player player;
        private World world;
        private Material type;

        AddExpiryToAllItemsInInventory(Player player, World world, Material type) {
            this.player = player;
            this.world = world;
            this.type = type;
        }

        @Override
        public void run() {
            Collection<ItemStack> allItems = (Collection<ItemStack>)player.getInventory().all(type).values();
            for (ItemStack item : allItems) {
                foodExpiryManager.addExpiry(item, world);
            }

            player.updateInventory();
        }
    }

    private final class PickupFoodListener implements Listener {

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onFoodPickupEvent(EntityPickupItemEvent e) {
            if (!e.getEntityType().equals(EntityType.PLAYER)) return;
            ItemStack itemStack = e.getItem().getItemStack();
            foodExpiryManager.addExpiry(itemStack, e.getEntity().getWorld());
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onGetPreparedFoodEvent(InventoryClickEvent e) {
            List<HumanEntity> viewers = e.getViewers();
            if (viewers.size() <= 0) return;

            ItemStack currentItem = e.getCurrentItem();
            if (currentItem == null) return;

            World world = e.getViewers().get(0).getWorld();

            Player player = (Player)viewers.get(0);

            foodExpiryManager.addExpiry(currentItem, world);
            getServer().getScheduler().runTaskLater(FoodExpiry.this, new AddExpiryToAllItemsInInventory(player, world, currentItem.getType()), 1);
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public void onRightClickClockEvent(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Action action = event.getAction();
            ItemStack item = event.getItem();

             if (!(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)))
                 return;

             if (item == null) return;
             if (!item.getType().equals(Material.CLOCK)) return;

             String currentDate = foodExpiryManager.getCurrentDateStr(player.getWorld());
             player.sendMessage("current date " + currentDate);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandProcessor.process(sender, cmd, label, args);
    }
}
