package com.mllilek.foodexpiry;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class ExpiryFormatter {
    private final String expiryPrefix;
    private final SimpleDateFormat expiryFormat;

    ExpiryFormatter(Configuration config) {
        ConfigurationSection expirySection = config.getConfigurationSection("expiry");
        this.expiryPrefix = expirySection.getString("prefix");
        this.expiryFormat = new SimpleDateFormat(expirySection.getString("format"));
    }

    boolean isExpiryLine(String lore) {
        return lore.startsWith(expiryPrefix);
    }

    Instant parse(String expiryStr) {
        try {
            return expiryFormat.parse(expiryStr).toInstant();
        } catch (ParseException e) {
            return null;
        }
    }

    String generateExpiry(Instant time) {
        return expiryFormat.format(Date.from(time));
    }
}
