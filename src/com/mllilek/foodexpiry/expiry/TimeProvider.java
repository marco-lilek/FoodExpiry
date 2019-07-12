package com.mllilek.foodexpiry.expiry;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class TimeProvider {
    public static final int TICKS_IN_DAY = 24000;
    public static final int TICKS_IN_SECOND = 20;

    public final Instant startDate;

    public TimeProvider(ConfigurationSection config) {
        int day = config.getInt("day");
        int month = config.getInt("month");
        int year = config.getInt("year");

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DATE, day);

        this.startDate = calendar.toInstant();
    }


    public Instant getWorldTime(World world) {
        long secondsSinceWorldBegin = TimeUnit.DAYS.toSeconds(world.getFullTime() / TICKS_IN_DAY);
        return startDate.plusSeconds(secondsSinceWorldBegin);
    }
}
