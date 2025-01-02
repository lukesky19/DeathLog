package com.github.lukesky19.deathLog.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InventoryManager {

    public List<byte[]> serializeInventory(Inventory inventory) {
        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> !itemStack.getType().equals(Material.AIR))
                .map(ItemStack::serializeAsBytes)
                .toList();
    }

    public List<ItemStack> deserializeInventory(List<byte[]> inventoryBytes) {
        return inventoryBytes.stream().map(ItemStack::deserializeBytes).toList();
    }

    public void restoreInventory(Player player, List<ItemStack> items) {
        Inventory inventory = player.getInventory();

        items.forEach(itemStack -> {
            @NotNull HashMap<Integer, ItemStack> leftover = inventory.addItem(itemStack);

            for(Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
                ItemStack item = entry.getValue();

                player.getWorld().dropItem(player.getLocation(), item);
            }
        });
    }

    public void restoreExp(Player player, int exp) {
        player.giveExp(exp);
    }
}
