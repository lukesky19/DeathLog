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
package com.github.lukesky19.deathLog.listener;

import com.github.lukesky19.deathLog.DeathLog;
import com.github.lukesky19.deathLog.manager.InventoryManager;
import com.github.lukesky19.deathLog.config.player.PlayerData;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class listens to when a player dies and logs deaths.
 */
public class PlayerDeathListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull InventoryManager inventoryManager;
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param deathLog A {@link DeathLog} instance.
     * @param inventoryManager An {@link InventoryManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public PlayerDeathListener(
            @NotNull DeathLog deathLog,
            @NotNull InventoryManager inventoryManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.logger = deathLog.getComponentLogger();
        this.inventoryManager = inventoryManager;
        this.playerDataManager = playerDataManager;
    }

    /**
     * Listens to when a player dies and logs the death.
     * @param playerDeathEvent A {@link PlayerDeathEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        // Get the Player involved in the death
        Player player = playerDeathEvent.getPlayer();
        // Get the Player's UUID
        UUID uuid = player.getUniqueId();
        // Get the Player's Inventory
        Inventory inventory = player.getInventory();
        // Get the Player's death playerDataLocation.
        Location deathLocation = player.getLastDeathLocation();
        // Get the reason the Player died.
        Component deathReasonComponent = Objects.requireNonNullElse(playerDeathEvent.deathMessage(), AdventureUtil.serialize(("<red>Unknown Death Message.</red>")));
        // Get the Player's experience
        int exp = player.getTotalExperience();

        // Serialize the component to a string.
        String deathReasonString = MiniMessage.miniMessage().serialize(deathReasonComponent);
        // Serialize the inventory contents into bytes.
        List<byte[]> bytes = inventoryManager.serializeInventory(inventory);
        // Get the Player's PlayerData
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        // Get the Player's current logged deaths or create a new list.
        List<PlayerData.Entry> entryList = playerData.entries();

        // Create the location where the player died
        PlayerData.Entry entry;
        if(deathLocation != null && deathLocation.getWorld() != null) {
            // Create the PlayerData Location where the Player died.
            PlayerData.Location playerDataLocation = new PlayerData.Location(deathLocation.getWorld().getName(), deathLocation.x(), deathLocation.y(), deathLocation.z());

            entry = new PlayerData.Entry(System.currentTimeMillis(), deathReasonString, playerDataLocation, bytes, exp);
        } else {
            entry = new PlayerData.Entry(System.currentTimeMillis(), deathReasonString, new PlayerData.Location(null, null, null, null), bytes, exp);
        }

        // Add the entry to the list
        entryList.add(entry);

        // Save the updated PlayerData
        playerDataManager.savePlayerData(player.getName(), uuid, playerData);

        // Create the message to log the death to console
        String logMessage;
        if(deathLocation != null && deathLocation.getWorld() != null) {
            logMessage = "<yellow>Player died at" +
                    " x: <red>" + deathLocation.x() +
                    "</red> y: <red>" + deathLocation.y() +
                    "</red> z: <red>" + deathLocation.z() +
                    "</red> in world: <red>" + deathLocation.getWorld().getName() +
                    "</red>. Reason: <red>" + deathReasonString + "</red>.</yellow>";
        } else {
            logMessage = "<yellow>Player died at an unknown location. Reason: <red>" + deathReasonString + "</red>.</yellow>";
        }

        // Send the death log message.
        logger.info(AdventureUtil.serialize((logMessage)));
    }
}
