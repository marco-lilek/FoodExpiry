package com.mllilek.foodexpiry.expiry;

import org.bukkit.World;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class TimeProvider {
    public static final int TICKS_IN_DAY = 24000;
    public static final int TICKS_IN_SECOND = 20;
    public static final int TICKS_IN_MINUTE = 20;

    public Instant getWorldTime(World world) {
        long daysSinceEpoch = TimeUnit.DAYS.toSeconds(world.getFullTime() / TICKS_IN_DAY);
        return Instant.ofEpochSecond(daysSinceEpoch);
    }
}
