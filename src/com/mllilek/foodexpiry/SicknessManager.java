package com.mllilek.foodexpiry;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SicknessManager {

    private class RangeConfig {
        int min;
        int max;
        RangeConfig(ConfigurationSection config) {
            this.min = config.getInt("min");
            this.max = config.getInt("max");
        }
    }

    private final Random random;
    private final double chanceOfGettingSick;
    private final List<String> possibleEffects;

    private final RangeConfig effectDurationRange;
    private final RangeConfig hungerDurationRange;

    SicknessManager(Configuration config, Random random) {
        ConfigurationSection sicknessConfig = config.getConfigurationSection("sickness");
        this.random = random;
        this.chanceOfGettingSick = sicknessConfig.getDouble("chanceOfGettingSick");
        this.possibleEffects = sicknessConfig
                .getList("possibleEffects")
                .stream()
                .map(x -> (String)x)
                .collect(Collectors.toList());

        this.effectDurationRange = new RangeConfig(config.getConfigurationSection("effectDuration"));
        this.hungerDurationRange = new RangeConfig(config.getConfigurationSection("hungerDuration"));
    }

    boolean maybeMakePlayerSick(Player ply) {
        if (random.nextDouble() > chanceOfGettingSick) {
            return false;
        }

        ply.setSaturation(0);
        {
            String effectToApply = possibleEffects.get(MathHelper.range(
                    random.nextInt(), 0 /* min */, possibleEffects.size()));
            PotionEffectType effectType = PotionEffectType.getByName(effectToApply);
            int effectDuration = MathHelper.range(
                    random.nextInt(), effectDurationRange.min, effectDurationRange.max);

            ply.addPotionEffect(new PotionEffect(
                    effectType,
                    effectDuration * TimeManager.TICKS_IN_SECOND,
                    random.nextInt(3) /* amplifier */));
        }

        {
            int hungerDuration = MathHelper.range(
                    random.nextInt(), hungerDurationRange.min, hungerDurationRange.max);
            ply.addPotionEffect(new PotionEffect(
                    PotionEffectType.HUNGER,
                    hungerDuration,
                    random.nextInt(3)));
        }

        return true;
    }
}
