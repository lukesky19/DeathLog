package com.github.lukesky19.deathLog.config.player;

import com.github.lukesky19.deathLog.DeathLog;
import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.configurate.CommentedConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.CheckForNull;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerDataManager {
    private final DeathLog deathLog;

    public PlayerDataManager(DeathLog deathLog) {
        this.deathLog = deathLog;
    }

    @CheckForNull
    public PlayerData getPlayerData(UUID uuid) {
        PlayerData playerData;
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + uuid + ".yml");

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            playerData = loader.load().get(PlayerData.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        return playerData;
    }

    public void savePlayerSettings(String playerName, UUID uuid, PlayerData playerData) {
        Path path = Path.of(deathLog.getDataFolder() + File.separator + "playerdata" + File.separator + uuid + ".yml");
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerData);
            loader.save(playerNode);
        } catch(ConfigurateException e) {
            deathLog.getComponentLogger().error(FormatUtil.format("<red>Unable to save " + playerName + "'s settings.</red>"));
            throw new RuntimeException(e);
        }
    }
}
