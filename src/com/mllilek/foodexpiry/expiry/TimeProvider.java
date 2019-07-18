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
    private final boolean useRealWorldDate;

    public TimeProvider(ConfigurationSection config) {
        useRealWorldDate = config.getBoolean("useRealWorldDate");
        ConfigurationSection startDateSection = config.getConfigurationSection("startDate");
        if (useRealWorldDate) {
            startDate = null;
        } else {
            if (startDateSection != null) {
                int day = startDateSection.getInt("day");
                int month = startDateSection.getInt("month");
                int year = startDateSection.getInt("year");

                Calendar calendar = Calendar.getInstance();
                calendar.clear();

                // from 1-indexed to JAN-indexed
                calendar.set(Calendar.MONTH, month - 1 + Calendar.JANUARY);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.DATE, day);

                startDate = calendar.toInstant();
            } else {
                startDate = null;
            }
        }
    }


    public Instant getWorldTime(World world) {
        if (useRealWorldDate) {
            return Instant.now();
        }

        Instant startDateToUse = startDate == null ? Instant.EPOCH : startDate;
        long secondsSinceWorldBegin = TimeUnit.DAYS.toSeconds(world.getFullTime() / TICKS_IN_DAY);
        return startDateToUse.plusSeconds(secondsSinceWorldBegin);
    }
}
