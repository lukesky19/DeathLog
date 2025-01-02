package com.github.lukesky19.deathLog.listener;

import com.github.lukesky19.deathLog.DeathLog;
import com.github.lukesky19.deathLog.manager.InventoryManager;
import com.github.lukesky19.deathLog.config.player.PlayerData;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.skylib.format.FormatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class PlayerDeathListener implements Listener {
    private final DeathLog deathLog;
    private final InventoryManager inventoryManager;
    private final PlayerDataManager playerDataManager;

    public PlayerDeathListener(DeathLog deathLog, InventoryManager inventoryManager, PlayerDataManager playerDataManager) {
        this.deathLog = deathLog;
        this.inventoryManager = inventoryManager;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Get the Player involved in the death
        Player player = event.getPlayer();
        // Get the Player's UUID
        UUID uuid = player.getUniqueId();
        // Get the Player's Inventory
        Inventory inventory = player.getInventory();
        // Get the Player's death playerDataLocation.
        Location deathLocation = player.getLastDeathLocation();
        // Get the reason the Player died.
        Component deathReasonComponent = event.deathMessage();
        // Get the Player's experience
        int exp = player.getTotalExperience();

        // Don't log the death of the location, world, or reason are null
        if(deathLocation == null || deathLocation.getWorld() == null || deathReasonComponent == null) return;

        // Serialize the component to a string.
        String deathReasonString = MiniMessage.miniMessage().serialize(deathReasonComponent);
        // Serialize the inventory contents into bytes.
        List<byte[]> bytes = inventoryManager.serializeInventory(inventory);
        // Get the Player's PlayerData
        PlayerData playerData = playerDataManager.getPlayerData(uuid) == null ? new PlayerData(new ArrayList<>()) : playerDataManager.getPlayerData(uuid);

        // Get the Player's current logged deaths or create a new list.
        assert playerData != null; // PlayerData is checked for null above
        List<PlayerData.Entry> entryList = playerData.entries() == null ? new ArrayList<>() : playerData.entries();

        // Create the PlayerData Location where the Player died.
        PlayerData.Location playerDataLocation = new PlayerData.Location(deathLocation.getWorld().getName(), deathLocation.x(), deathLocation.y(), deathLocation.z());
        // Create a new entry for this Death.
        PlayerData.Entry entry = new PlayerData.Entry(System.currentTimeMillis(), deathReasonString, playerDataLocation, bytes, exp);

        // Add the entry to the list
        entryList.add(entry);

        // Create a new PlayerData object
        PlayerData updatedPlayerData = new PlayerData(entryList);

        // Save the new PlayerData
        playerDataManager.savePlayerSettings(player.getName(), uuid, updatedPlayerData);

        // Create the message to log the death to console
        String logMessage = "<yellow>Player died at" +
                " x: <red>" + deathLocation.x() +
                "</red> y: <red>" + deathLocation.y() +
                "</red> z: <red>" + deathLocation.z() +
                "</red> in world: <red>" + deathLocation.getWorld().getName() +
                "</red>. Reason: <red>" + deathReasonString + "</red>.</yellow>";

        // Send the death log message.
        deathLog.getComponentLogger().info(FormatUtil.format(logMessage));
    }
}
