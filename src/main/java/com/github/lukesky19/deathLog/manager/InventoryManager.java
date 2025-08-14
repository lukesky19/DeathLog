/*
    DeathLog logs player death locations, reasons, inventories, and experience. Inventories and experience can be restored.
    Copyright (C) 2025 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.deathLog.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class manages the serialization and deserialization of an {@link Inventory}'s contents.
 * Also manages the restoring of the {@link Inventory}'s contents and exp the player had at the time of death.
 */
public class InventoryManager {
    /**
     * Constructor
     */
    public InventoryManager() {}

    /**
     * Serializes the {@link Inventory}'s to a {@link List} of byte arrays.
     * @param inventory The {@link Inventory} to serialize.
     * @return A {@link List} of byte arrays.
     */
    public @NotNull List<byte[]> serializeInventory(Inventory inventory) {
        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> !itemStack.isEmpty())
                .map(ItemStack::serializeAsBytes)
                .toList();
    }

    /**
     * Deserializes a {@link List} of byte arrays to a {@link List} of {@link ItemStack}s.
     * @param inventoryBytes The {@link List} of byte arrays to deserialize.
     * @return A {@link List} of {@link ItemStack}s.
     */
    public @NotNull List<@NotNull ItemStack> deserializeInventory(List<byte[]> inventoryBytes) {
        return inventoryBytes.stream().map(ItemStack::deserializeBytes).toList();
    }

    /**
     * Give the {@link List} of {@link ItemStack}s to the provided {@link Player}.
     * @param player The {@link Player} to give the items.
     * @param items The {@link List} of {@link ItemStack}s to give.
     */
    public void giveItems(@NotNull Player player, @NotNull List<ItemStack> items) {
        Inventory inventory = player.getInventory();

        items.forEach(itemStack -> {
            @NotNull HashMap<Integer, ItemStack> leftover = inventory.addItem(itemStack);

            for(Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
                ItemStack item = entry.getValue();

                player.getWorld().dropItem(player.getLocation(), item);
            }
        });
    }

    /**
     * Give the experience to the {@link Player} provided.
     * @param player The {@link Player} to give experience to.
     * @param exp The experience to give.
     */
    public void giveExp(Player player, int exp) {
        player.giveExp(exp);
    }
}
