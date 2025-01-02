package com.github.lukesky19.deathLog;

import com.github.lukesky19.deathLog.commands.DeathLogCommand;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.deathLog.listener.PlayerDeathListener;
import com.github.lukesky19.deathLog.manager.InventoryManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathLog extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        PlayerDataManager playerDataManager = new PlayerDataManager(this);
        InventoryManager inventoryManager = new InventoryManager();
        DeathLogCommand deathLogCommand = new DeathLogCommand(inventoryManager, playerDataManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(deathLogCommand.createCommand(),
                        "Command to manage the DeathLog plugin."));

        PlayerDeathListener playerDeathListener = new PlayerDeathListener(this, inventoryManager, playerDataManager);

        this.getServer().getPluginManager().registerEvents(playerDeathListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
