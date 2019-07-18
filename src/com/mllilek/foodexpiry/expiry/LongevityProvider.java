package com.mllilek.foodexpiry.expiry;

import com.mllilek.foodexpiry.CollectionsHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

class LongevityProvider {
    private final int defaultExpiry;
    private final Map<String, Integer> foodsExpiryMap;


    LongevityProvider(int defaultExpiry, Map<String, Integer> foodsExpiryMap) {
        this.defaultExpiry = defaultExpiry;
        this.foodsExpiryMap = foodsExpiryMap;
        validateExpiryMap();
    }

    public static LongevityProvider fromConfig(ConfigurationSection longevitySection) {
        Map<String, Integer> foodExpiryMap = new HashMap<>();

        for (Object mapEntry: longevitySection.getList("foodExpiryMap")) {
            List<Object> asList = (List<Object>)mapEntry;
            String type = (String) asList.get(0);
            Integer val  = (Integer)asList.get(1);
            foodExpiryMap.put(type, val);
        }
        return new LongevityProvider(longevitySection.getInt("defaultExpiry"), foodExpiryMap);
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
