package com.mllilek.foodexpiry;

import org.bukkit.World;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class TimeManager {
    static final int TICKS_IN_DAY = 24000;
    static final int TICKS_IN_SECOND = 20;
    static final int TICKS_IN_MINUTE = 20;

    Instant getWorldTime(World world) {
        long daysSinceEpoch = TimeUnit.DAYS.toSeconds(world.getFullTime() / TICKS_IN_DAY);
        return Instant.ofEpochSecond(daysSinceEpoch);
    }
}
