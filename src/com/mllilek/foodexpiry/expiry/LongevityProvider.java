package com.mllilek.foodexpiry.expiry;

import com.mllilek.foodexpiry.CollectionsHelper;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.stream.Collectors;

class LongevityProvider {
    private final int defaultExpiry;
    private final Map<String, Integer> foodsExpiryMap;

    LongevityProvider(int defaultExpiry, Map<String, Integer> foodsExpiryMap) {
        this.defaultExpiry = defaultExpiry;
        this.foodsExpiryMap = foodsExpiryMap;
    }

    public static LongevityProvider fromConfig(ConfigurationSection longevitySection) {
        ConfigurationSection foodExpiryMap = longevitySection.getConfigurationSection("foodExpiryMap");
        return new LongevityProvider(
                longevitySection.getInt("defaultExpiry"),
                CollectionsHelper.castMap(foodExpiryMap.getValues(false)));
    }

    Integer getLongevity(Material type) {
        String foodType = type.getClass().toString();
        return foodsExpiryMap.getOrDefault(foodType, defaultExpiry);
    }
}
