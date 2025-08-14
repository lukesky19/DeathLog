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
package com.github.lukesky19.deathLog;

import com.github.lukesky19.deathLog.commands.DeathLogCommand;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.deathLog.listener.PlayerDeathListener;
import com.github.lukesky19.deathLog.manager.InventoryManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin's class.
 */
public final class DeathLog extends JavaPlugin {
    /**
     * Default Constructor
     */
    public DeathLog() {}

    /**
     * The method ran when the plugin is enabled.
     */
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
}
