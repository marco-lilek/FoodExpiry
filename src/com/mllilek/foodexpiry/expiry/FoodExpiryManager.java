package com.mllilek.foodexpiry.expiry;

import com.mllilek.foodexpiry.ConfigHelper;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FoodExpiryManager {
    private final LongevityProvider longevityProvider;
    private final ExpiryFormatter expiryFormatter;
    private final TimeProvider timeProvider;

    private final boolean debugAlwaysTriggerExpiry;

    public static FoodExpiryManager fromConfig(ConfigurationSection config) {
        ConfigurationSection formatSection = config.getConfigurationSection("format");
        ConfigurationSection longevitySection = config.getConfigurationSection("longevity");
        ConfigurationSection timeSection = config.getConfigurationSection("startDate");

        ExpiryFormatter expiryFormatter = ExpiryFormatter.fromConfig(formatSection);
        LongevityProvider longevityProvider = LongevityProvider.fromConfig(longevitySection);
        TimeProvider timeProvider = new TimeProvider(timeSection);

        return new FoodExpiryManager(longevityProvider,
                expiryFormatter,
                timeProvider,
                config.getBoolean("alwaysTrigger"));
    }

    FoodExpiryManager(LongevityProvider longevityProvider,
                      ExpiryFormatter expiryFormatter,
                      TimeProvider timeProvider,
                      boolean debugAlwaysTriggerExpiry) {
        this.longevityProvider = longevityProvider;
        this.expiryFormatter = expiryFormatter;
        this.timeProvider = timeProvider;
        this.debugAlwaysTriggerExpiry = debugAlwaysTriggerExpiry;
    }

    public void addExpiry(ItemStack itemStack, World world) {
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

        Instant expiryTime = timeProvider.getWorldTime(world).plus(itemLongevity, ChronoUnit.DAYS);
        String expiry = expiryFormatter.generateExpiry(expiryTime);
        itemLore.add(expiry);

        itemMeta.setLore(itemLore);
        itemStack.setItemMeta(itemMeta);
    }

    public boolean isExpired(ItemStack itemStack, World world) {
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
        return timeProvider.getWorldTime(world).compareTo(expiryTime) >= 0;
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

    public String getCurrentDateStr(World world) {
        return expiryFormatter.formatTime(timeProvider.getWorldTime(world));
    }
}
