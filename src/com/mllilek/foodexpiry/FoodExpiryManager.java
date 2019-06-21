package com.mllilek.foodexpiry;

import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class FoodExpiryManager {
    private final LongevityProvider longevityProvider;
    private final ExpiryFormatter expiryFormatter;
    private final TimeManager timeManager;

    private final boolean debugAlwaysTriggerExpiry;

    FoodExpiryManager(LongevityProvider longevityProvider,
                      ExpiryFormatter expiryFormatter,
                      TimeManager timeManager,
                      Configuration config) {
        this.longevityProvider = longevityProvider;
        this.expiryFormatter = expiryFormatter;
        this.timeManager = timeManager;

        ConfigurationSection debugSection = config.getConfigurationSection(ConfigHelper.DEBUG_SECTION);
        this.debugAlwaysTriggerExpiry = debugSection.getBoolean("alwaysTriggerExpiry");
    }

    void addExpiry(ItemStack itemStack, World world) {
        if (!itemStack.getType().isEdible()) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) {
            itemLore = new ArrayList<>();
        }

        Integer expiryLineIdxInLore = findExpiryInLore(itemLore);
        // Never change the expiry on an item
        if (expiryLineIdxInLore != null) {
            return;
        }

        if (itemStack.getItemMeta() == null) {
            return;
        }

        Integer itemLongevity = longevityProvider.getLongevity(itemStack.getType());
        if (itemLongevity == null) {
            return;
        }

        Instant worldTime = timeManager.getWorldTime(world);
        String expiry = expiryFormatter.generateExpiry(worldTime);
        itemLore.add(expiry);

        itemMeta.setLore(itemLore);
        itemStack.setItemMeta(itemMeta);
    }

    boolean isExpired(ItemStack itemStack, World world) {
        if (debugAlwaysTriggerExpiry) {
            return true;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        List<String> itemLore = itemMeta.getLore();
        if (itemLore == null) {
            return false;
        }

        Integer expiryLineIdxInLore = findExpiryInLore(itemLore);
        if (expiryLineIdxInLore == null) {
            return false;
        }

        Instant expiryTime = expiryFormatter.parse(itemLore.get(expiryLineIdxInLore));
        return timeManager.getWorldTime(world).compareTo(expiryTime) >= 0;
    }

    private Integer findExpiryInLore(List<String> itemLore) {
        for (int i = 0; i < itemLore.size(); i++) {
            String lore = itemLore.get(i);
            if (expiryFormatter.isExpiryLine(lore)) {
                return i;
            }
        }

        return null;
    }

    String getCurrentDateStr(World world) {
        return expiryFormatter.generateExpiry(timeManager.getWorldTime(world));
    }
}
