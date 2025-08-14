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
package com.github.lukesky19.deathLog.config.player;

import com.github.lukesky19.deathLog.DeathLog;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This class manages the loading and saving of player data.
 */
public class PlayerDataManager {
    private final @NotNull DeathLog deathLog;

    /**
     * Constructor
     * @param deathLog A {@link DeathLog} instance.
     */
    public PlayerDataManager(@NotNull DeathLog deathLog) {
        this.deathLog = deathLog;
    }

    /**
     * Get the player data for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link PlayerData}
     */
    public @NotNull PlayerData getPlayerData(@NotNull UUID uuid) {
        PlayerData playerData;
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + uuid + ".yml");
        if(!path.toFile().exists()) return new PlayerData(new ArrayList<>());

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            playerData = loader.load().get(PlayerData.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        if(playerData != null) return playerData;
        return new PlayerData(new ArrayList<>());
    }

    /**
     * Save the player data.
     * @param playerName The player's name.
     * @param uuid The player's {@link UUID}.
     * @param playerData The {@link PlayerData} to save.
     */
    public void savePlayerData(@NotNull String playerName, @NotNull UUID uuid, @NotNull PlayerData playerData) {
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + uuid + ".yml");
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerData);
            loader.save(playerNode);
        } catch(ConfigurateException e) {
            deathLog.getComponentLogger().error(AdventureUtil.serialize(("<red>Unable to save " + playerName + "'s settings.</red>")));
            throw new RuntimeException(e);
        }
    }
}
