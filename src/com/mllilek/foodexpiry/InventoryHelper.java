package com.mllilek.foodexpiry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class InventoryHelper {

    private static boolean isSoup(Material mat) {
        return mat.equals(Material.BEETROOT_SOUP) ||
                mat.equals(Material.MUSHROOM_STEW) ||
                mat.equals(Material.RABBIT_STEW);
    }

    static void removeItemFromMainHand(Player ply) {
        // Remove the item from the players inventory
        ItemStack itemStack = ply.getInventory().getItemInMainHand();
        if (isSoup(itemStack.getType())) {
            ply.getInventory().setItemInMainHand(
                    new ItemStack(Material.BOWL, 1));
        } else {
            if (itemStack.getAmount() <= 1) {
                ply.getInventory().removeItem(itemStack);
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
                ply.getInventory().setItemInMainHand(itemStack);
            }
        }
    }
}
