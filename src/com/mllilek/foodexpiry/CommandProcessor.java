package com.mllilek.foodexpiry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {
    private final String commandName;

    CommandProcessor(String commandName) {
        this.commandName = commandName;
    }
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase(commandName)) return true;
        if (args.length != 0) return false;
        if (!(sender instanceof Player)) return false;

        Player ply = (Player) sender;
        List<String> msg = new ArrayList<>();
        msg.add("# === Food Expiry ===");
        msg.add("# Avoid eating expired foods, they might make you sick.");
        msg.add("# ");
        msg.add("# Bread, fish, and soups last long");
        msg.add("# ");
        msg.add("# Right click with a clock to get the current world date");
        msg.add("# ===================");

        msg.forEach(ply::sendMessage);
        return true;
    }
}
