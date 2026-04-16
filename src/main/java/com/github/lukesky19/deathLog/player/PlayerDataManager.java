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
package com.github.lukesky19.deathLog.player;

import com.github.lukesky19.deathLog.DeathLog;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This class manages the loading and saving of player data.
 */
public class PlayerDataManager {
    private final @NonNull DeathLog deathLog;
    private final @NonNull ComponentLogger logger;

    /**
     * Constructor
     * @param deathLog A {@link DeathLog} instance.
     */
    public PlayerDataManager(@NonNull DeathLog deathLog) {
        this.deathLog = deathLog;
        this.logger = deathLog.getComponentLogger();
    }

    /**
     * Get the player data for the {@link UUID} provided.
     * @param playerId The {@link UUID} of the player.
     * @return The {@link PlayerData} or null if loading failed.
     */
    public @Nullable PlayerData getPlayerData(@NonNull UUID playerId) {
        PlayerData playerData;
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + playerId + ".yml");
        if(!path.toFile().exists()) return new PlayerData(1, new ArrayList<>());

        YamlConfigurationLoader loader = createLoader(path);
        try {
            playerData = loader.load().get(PlayerData.class);

            if(playerData != null) {
                if(playerData.version() == 0) {
                    playerData = new PlayerData(1, playerData.entries());

                    savePlayerData(playerId, playerData);
                }
            } else {
                playerData = new PlayerData(1, new ArrayList<>());
                
                savePlayerData(playerId, playerData);
            }
            
            return playerData;
        } catch (ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to load player data for player " + playerId));
            return null;
        }
    }

    /**
     * Save the player data.
     * @param playerName The player's name.
     * @param playerId The player's {@link UUID}.
     * @param playerData The {@link PlayerData} to save.
     */
    public void savePlayerData(@NonNull String playerName, @NonNull UUID playerId, @NonNull PlayerData playerData) {
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + playerId + ".yml");
        YamlConfigurationLoader loader = createLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerData);
            loader.save(playerNode);
        } catch(ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save player data for player " + playerName));
        }
    }

    /**
     * Save the player data.
     * @param playerId The player's {@link UUID}.
     * @param playerData The {@link PlayerData} to save.
     */
    public void savePlayerData(@NonNull UUID playerId, @NonNull PlayerData playerData) {
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + playerId + ".yml");
        YamlConfigurationLoader loader = createLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerData);
            loader.save(playerNode);
        } catch(ConfigurateException e) {
            logger.error(AdventureUtility.plain("Failed to save player data for player " + playerId));
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .build();
    }
}