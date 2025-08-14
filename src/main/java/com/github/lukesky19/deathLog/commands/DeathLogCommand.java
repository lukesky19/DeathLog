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
package com.github.lukesky19.deathLog.commands;

import com.github.lukesky19.deathLog.manager.InventoryManager;
import com.github.lukesky19.deathLog.config.player.PlayerData;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class creates the /deathlog command to register.
 */
public class DeathLogCommand {
    private final @NotNull InventoryManager inventoryManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull SimpleDateFormat simpleDateFormat;

    /**
     * Constructor
     * @param inventoryManager An {@link InventoryManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public DeathLogCommand(
            @NotNull InventoryManager inventoryManager,
            @NotNull PlayerDataManager playerDataManager) {
        this.inventoryManager = inventoryManager;
        this.playerDataManager = playerDataManager;

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("deathlog")
                .requires(ctx -> ctx.getSender().hasPermission("deathlog.command.deathlog"));

        builder.then(Commands.literal("restore")
            .requires(ctx -> ctx.getSender().hasPermission("deathlog.command.deathlog.restore"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("id", IntegerArgumentType.integer())
                    .suggests((ctx, suggestionsBuilder) -> {
                        Player target = ctx.getLastChild().getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                        UUID targetUUID = target.getUniqueId();
                        PlayerData playerData = playerDataManager.getPlayerData(targetUUID);

                        List<PlayerData.Entry> entryList = playerData.entries();

                        for(PlayerData.Entry entry : entryList) {
                            int id = entryList.indexOf(entry);

                            Date date = new Date(entry.time());

                            Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.serialize(("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>")));

                            suggestionsBuilder.suggest(id, toolTip);
                        }

                        return suggestionsBuilder.buildFuture();
                    })

                    .then(Commands.literal("inventory")
                        .executes(ctx -> {
                            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = target.getUniqueId();
                            int id = ctx.getArgument("id", int.class);

                            PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                            if(id < 0 || id >= playerData.entries().size()) {
                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                return 0;
                            }

                            PlayerData.Entry entry = playerData.entries().get(id);
                            if (entry != null) {
                                List<ItemStack> itemStacks = inventoryManager.deserializeInventory(entry.items());

                                inventoryManager.giveItems(target, itemStacks);

                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>Restored the inventory for " + target.getName() + ".</red>")));

                                return 1;
                            } else {
                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                return 0;
                            }
                        })
                    )

                    .then(Commands.literal("exp")
                        .executes(ctx -> {
                            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = target.getUniqueId();
                            int id = ctx.getArgument("id", int.class);

                            PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                            if(id < 0 || id >= playerData.entries().size()) {
                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                return 0;
                            }

                            PlayerData.Entry entry = playerData.entries().get(id);
                            if (entry != null) {
                                inventoryManager.giveExp(target, entry.exp());

                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>Restored the experience for " + target.getName() + ".</red>")));

                                return 1;
                            } else {
                                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                return 0;
                            }
                        })
                    )
                )
            )
        );

        builder.then(Commands.literal("give")
            .requires(ctx -> ctx.getSender().hasPermission("deathlog.command.deathlog.restore"))
            .then(Commands.argument("sender", ArgumentTypes.player())
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("id", IntegerArgumentType.integer())
                        .suggests((ctx, suggestionsBuilder) -> {
                            Player target = ctx.getLastChild().getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = target.getUniqueId();
                            PlayerData playerData = playerDataManager.getPlayerData(targetUUID);

                            List<PlayerData.Entry> entryList = playerData.entries();

                            for(PlayerData.Entry entry : entryList) {
                                int id = entryList.indexOf(entry);

                                Date date = new Date(entry.time());

                                Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.serialize(("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>")));

                                suggestionsBuilder.suggest(id, toolTip);
                            }

                            return suggestionsBuilder.buildFuture();
                        })

                        .then(Commands.literal("inventory")
                            .executes(ctx -> {
                                Player sender = ctx.getArgument("sender", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                UUID targetUUID = target.getUniqueId();
                                int id = ctx.getArgument("id", int.class);

                                PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                                if(id < 0 || id >= playerData.entries().size()) {
                                    ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                    return 0;
                                }

                                PlayerData.Entry entry = playerData.entries().get(id);
                                if (entry != null) {
                                    List<ItemStack> itemStacks = inventoryManager.deserializeInventory(entry.items());

                                    if(itemStacks.isEmpty()) ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("Items is empty!")));

                                    inventoryManager.giveItems(sender, itemStacks);

                                    ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>Given the inventory for " + target.getName() + " to " + sender.getName() + ".</red>")));

                                    return 1;
                                } else {
                                    ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                                    return 0;
                                }
                            })
                        )
                    )
                )
            )
        );
        
        builder.then(Commands.literal("info")
            .requires(ctx -> ctx.getSender().hasPermission("deathlog.command.deathlog.info"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("id", IntegerArgumentType.integer())
                    .suggests((ctx, suggestionsBuilder) -> {
                        Player target = ctx.getLastChild().getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                        UUID targetUUID = target.getUniqueId();
                        PlayerData playerData = playerDataManager.getPlayerData(targetUUID);

                        List<PlayerData.Entry> entryList = playerData.entries();

                        for(PlayerData.Entry entry : entryList) {
                            int id = entryList.indexOf(entry);

                            Date date = new Date(entry.time());

                            Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.serialize(("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>")));

                            suggestionsBuilder.suggest(id, toolTip);
                        }

                        return suggestionsBuilder.buildFuture();
                    })

                    .executes(ctx -> {
                        Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                        UUID targetUUID = target.getUniqueId();
                        int id = ctx.getArgument("id", int.class);

                        PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                        if(id < 0 || id >= playerData.entries().size()) {
                            ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                            return 0;
                        }

                        PlayerData.Entry entry = playerData.entries().get(id);
                        if (entry != null) {
                            List<ItemStack> itemStacks = inventoryManager.deserializeInventory(entry.items());
                            int numOfItems = itemStacks.size();
                            PlayerData.Location location = entry.location();

                            Date date = new Date(entry.time());

                            List<TagResolver.Single> placeholders = List.of(
                                    Placeholder.parsed("time", simpleDateFormat.format(date)),
                                    Placeholder.parsed("x", Objects.requireNonNullElse(String.valueOf(location.x()),"Unknown X Coordinate")),
                                    Placeholder.parsed("y", Objects.requireNonNullElse(String.valueOf(location.y()),"Unknown Y Coordinate")),
                                    Placeholder.parsed("z", Objects.requireNonNullElse(String.valueOf(location.z()),"Unknown Z Coordinate")),
                                    Placeholder.parsed("world", Objects.requireNonNullElse(location.world(), "Unknown World")),
                                    Placeholder.parsed("cause", entry.cause()),
                                    Placeholder.parsed("exp", String.valueOf(entry.exp()))
                            );

                            Component timeOfDeath = AdventureUtil.serialize(("<yellow>Time of death: " +
                                    "<red><time></red>.</yellow>"), placeholders);
                            Component causeOfDeath = AdventureUtil.serialize(("<yellow>Cause of death: " +
                                    "<red><cause></red>.</yellow>"), placeholders);
                            Component deathLocation = AdventureUtil.serialize(("<yellow>Death location: x: " +
                                    "<red><x></red> y: <red><y></red> z: <red><z></red> " +
                                    "in world <red><world></red>.</yellow>"), placeholders);

                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("<yellow>Player had the following items: ");

                            for(int i = 0; i <= numOfItems - 1; i++) {
                                ItemStack itemStack = itemStacks.get(i);

                                if(i == numOfItems - 1) {
                                    stringBuilder.append("and <red>")
                                            .append(itemStack.getType()).append(" x")
                                            .append(itemStack.getAmount());

                                    stringBuilder.append(".</yellow>");
                                } else {
                                    stringBuilder.append("<red>")
                                            .append(itemStack.getType()).append(" x")
                                            .append(itemStack.getAmount());

                                    stringBuilder.append("</red>, ");
                                }
                            }

                            Component items = AdventureUtil.serialize((stringBuilder.toString()));
                            Component exp = AdventureUtil.serialize(("<yellow>Exp at death: <red><exp></red>.</yellow>"), placeholders);

                            CommandSender sender = ctx.getSource().getSender();

                            sender.sendMessage(timeOfDeath);
                            sender.sendMessage(causeOfDeath);
                            sender.sendMessage(deathLocation);
                            sender.sendMessage(items);
                            sender.sendMessage(exp);

                            return 1;
                        } else {
                            ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(("<red>There is no data associated with that ID.</red>")));

                            return 0;
                        }
                    })
                )
            )
        );

        return builder.build();
    }
}
