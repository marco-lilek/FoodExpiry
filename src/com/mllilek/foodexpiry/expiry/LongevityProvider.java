package com.mllilek.foodexpiry.expiry;

import com.mllilek.foodexpiry.CollectionsHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

class LongevityProvider {
    private final int defaultExpiry;
    private final Map<String, Integer> foodsExpiryMap;

    LongevityProvider(int defaultExpiry, Map<String, Integer> foodsExpiryMap) {
        this.defaultExpiry = defaultExpiry;
        this.foodsExpiryMap = foodsExpiryMap;
        validateExpiryMap();
    }

    public static LongevityProvider fromConfig(ConfigurationSection longevitySection) {
        ConfigurationSection foodExpiryMap = longevitySection.getConfigurationSection("foodExpiryMap");
        return new LongevityProvider(
                longevitySection.getInt("defaultExpiry"),
                CollectionsHelper.castMap(foodExpiryMap.getValues(false)));
    }

    Integer getLongevity(Material type) {
        String foodType = type.toString();
        return foodsExpiryMap.getOrDefault(foodType, defaultExpiry);
    }

    private void validateExpiryMap() {
        Set<String> materials = Arrays.stream(Material.values()).map(Enum::toString).collect(Collectors.toSet());
        for (String longevityOverride : foodsExpiryMap.keySet()) {
            if (!materials.contains(longevityOverride)) {
                throw new RuntimeException(String.format("longevity override type %s is not a valid type", longevityOverride));
            }
        }
    }
}
