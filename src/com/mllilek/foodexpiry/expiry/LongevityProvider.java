package com.mllilek.foodexpiry.expiry;

import com.mllilek.foodexpiry.CollectionsHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class LongevityProvider {
    private final int defaultExpiry;
    private static final int NO_EXPIRY = -1;
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
        Integer foodExpiry = foodsExpiryMap.get(foodType);
        if (foodExpiry == null) {
            return defaultExpiry;
        }

        if (foodExpiry == NO_EXPIRY) {
            return null;
        }

        return foodExpiry;
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
