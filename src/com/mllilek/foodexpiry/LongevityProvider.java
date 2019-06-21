package com.mllilek.foodexpiry;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.stream.Collectors;

public class LongevityProvider {
    private final Map<String, Integer> foodsExpiryMap;
    private final int defaultExpiry;

    LongevityProvider(Configuration config) {
        ConfigurationSection longevityConfig = config.getConfigurationSection("longevity");
        defaultExpiry = longevityConfig.getInt("defaultExpiry");
        foodsExpiryMap = longevityConfig.getConfigurationSection("foodExpiryMap")
                .getValues(false /* deep */)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x->(Integer)x.getValue()));
    }

    Integer getLongevity(Material type) {
        String foodType = type.getClass().toString();
        return foodsExpiryMap.getOrDefault(foodType, defaultExpiry);
    }
}
