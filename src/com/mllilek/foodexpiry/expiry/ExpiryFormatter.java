package com.mllilek.foodexpiry.expiry;

import org.bukkit.configuration.ConfigurationSection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

class ExpiryFormatter {
    private final String expiryPrefix;
    private final SimpleDateFormat expiryFormat;
    private final boolean alwaysMakeExpired;

    ExpiryFormatter(
            String expiryPrefix,
            SimpleDateFormat expiryFormat,
            boolean alwaysMakeExpired) {
        this.expiryPrefix = expiryPrefix;
        this.expiryFormat = expiryFormat;
        this.alwaysMakeExpired = alwaysMakeExpired;
    }

    public static ExpiryFormatter fromConfig(ConfigurationSection formatSection) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatSection.getString("format"));

        return new ExpiryFormatter(
                formatSection.getString("prefix"),
                dateFormat,
                formatSection.getBoolean("alwaysMakeExpired"));
    }

    boolean isExpiryLine(String lore) {
        return lore.startsWith(expiryPrefix);
    }

    Instant parse(String expiryStr) {
        try {
            return expiryFormat.parse(expiryStr.substring(expiryPrefix.length())).toInstant();
        } catch (ParseException e) {
            return null;
        }
    }

    String generateExpiry(Instant time) {
        if (alwaysMakeExpired) {
            time = time.minus(10, ChronoUnit.DAYS);
        }
        return expiryPrefix + formatTime(time);
    }

    String formatTime(Instant time) {
        return expiryFormat.format(Date.from(time));
    }

}
