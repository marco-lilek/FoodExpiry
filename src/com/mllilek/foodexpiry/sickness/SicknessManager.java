package com.mllilek.foodexpiry.sickness;

import com.mllilek.foodexpiry.CollectionsHelper;
import com.mllilek.foodexpiry.MathHelper;
import com.mllilek.foodexpiry.expiry.TimeProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class SicknessManager {
    static class RangeConfig {
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

    private final boolean alwaysTriggerSickness;

    SicknessManager(
            Random random,
            double chanceOfGettingSick,
            List<String> possibleEffects,
            RangeConfig effectDurationRange,
            RangeConfig hungerDurationRange,
            boolean alwaysTriggerSickness) {
        this.random = random;
        this.chanceOfGettingSick = chanceOfGettingSick;
        this.possibleEffects = possibleEffects;
        this.effectDurationRange = effectDurationRange;
        this.hungerDurationRange = hungerDurationRange;
        this.alwaysTriggerSickness = alwaysTriggerSickness;
    }

    public static SicknessManager fromConfig(
            Random random,
            ConfigurationSection config) {
        List<String> possibleEffects = config.getStringList("possibleEffects");
        RangeConfig effectDurationRange = new RangeConfig(config.getConfigurationSection("effectDuration"));
        RangeConfig hungerDurationRange = new RangeConfig(config.getConfigurationSection("hungerDuration"));
        return new SicknessManager(random,
                config.getDouble("chanceOfGettingSick"),
                possibleEffects,
                effectDurationRange,
                hungerDurationRange,
                config.getBoolean("alwaysTrigger"));
    }

    public boolean maybeMakePlayerSick(Player ply) {
        if (!alwaysTriggerSickness && random.nextDouble() > chanceOfGettingSick) {
            return false;
        }

        ply.setSaturation(0);
        {
            String effectToApply = possibleEffects.get((int)MathHelper.range(
                    random.nextDouble(), 0 /* min */, possibleEffects.size()));
            PotionEffectType effectType = PotionEffectType.getByName(effectToApply);
            int effectDuration = (int)MathHelper.range(
                    random.nextDouble(), effectDurationRange.min, effectDurationRange.max);

            ply.addPotionEffect(new PotionEffect(
                    effectType,
                    effectDuration * TimeProvider.TICKS_IN_SECOND,
                    random.nextInt(3) /* amplifier */));
        }

        {
            int hungerDuration = (int)MathHelper.range(
                    random.nextDouble(), hungerDurationRange.min, hungerDurationRange.max);
            ply.addPotionEffect(new PotionEffect(
                    PotionEffectType.HUNGER,
                    hungerDuration * TimeProvider.TICKS_IN_SECOND,
                    random.nextInt(3)));
        }

        return true;
    }
}
