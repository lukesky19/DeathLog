package com.github.lukesky19.deathLog.commands;

import com.github.lukesky19.deathLog.manager.InventoryManager;
import com.github.lukesky19.deathLog.config.player.PlayerData;
import com.github.lukesky19.deathLog.config.player.PlayerDataManager;
import com.github.lukesky19.skylib.format.FormatUtil;
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

import java.text.SimpleDateFormat;
import java.util.*;

public class DeathLogCommand {
    private final InventoryManager inventoryManager;
    private final PlayerDataManager playerDataManager;
    private final SimpleDateFormat simpleDateFormat;

    public DeathLogCommand(InventoryManager inventoryManager, PlayerDataManager playerDataManager) {
        this.inventoryManager = inventoryManager;
        this.playerDataManager = playerDataManager;

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }

    public LiteralCommandNode<CommandSourceStack> createCommand() {
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

                        if(playerData != null) {
                            List<PlayerData.Entry> entryList = playerData.entries();

                            for(PlayerData.Entry entry : entryList) {
                                int id = entryList.indexOf(entry);

                                Date date = new Date(entry.time());

                                Message toolTip = MessageComponentSerializer.message().serialize(FormatUtil.format("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>"));

                                suggestionsBuilder.suggest(id, toolTip);
                            }
                        }

                        return suggestionsBuilder.buildFuture();
                    })

                    .then(Commands.literal("inventory")
                        .executes(ctx -> {
                            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetUUID = target.getUniqueId();
                            int id = ctx.getArgument("id", int.class);

                            PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                            if (playerData != null) {
                                if(id < 0 || id >= playerData.entries().size()) {
                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                    return 0;
                                }

                                PlayerData.Entry entry = playerData.entries().get(id);
                                if (entry != null) {
                                    List<ItemStack> itemStacks = inventoryManager.deserializeInventory(entry.items());

                                    inventoryManager.restoreInventory(target, itemStacks);

                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>Restored the inventory for " + target.getName() + ".</red>"));

                                    return 1;
                                } else {
                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                    return 0;
                                }
                            } else {
                                ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with this player.</red>"));

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
                            if (playerData != null) {
                                if(id < 0 || id >= playerData.entries().size()) {
                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                    return 0;
                                }

                                PlayerData.Entry entry = playerData.entries().get(id);
                                if (entry != null) {
                                    inventoryManager.restoreExp(target, entry.exp());

                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>Restored the experience for " + target.getName() + ".</red>"));

                                    return 1;
                                } else {
                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                    return 0;
                                }
                            } else {
                                ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with this player.</red>"));

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

                            if(playerData != null) {
                                List<PlayerData.Entry> entryList = playerData.entries();

                                for(PlayerData.Entry entry : entryList) {
                                    int id = entryList.indexOf(entry);

                                    Date date = new Date(entry.time());

                                    Message toolTip = MessageComponentSerializer.message().serialize(FormatUtil.format("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>"));

                                    suggestionsBuilder.suggest(id, toolTip);
                                }
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
                                if (playerData != null) {
                                    if(id < 0 || id >= playerData.entries().size()) {
                                        ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                        return 0;
                                    }

                                    PlayerData.Entry entry = playerData.entries().get(id);
                                    if (entry != null) {
                                        List<ItemStack> itemStacks = inventoryManager.deserializeInventory(entry.items());

                                        if(itemStacks.isEmpty()) ctx.getSource().getSender().sendMessage(FormatUtil.format("Items is empty!"));

                                        inventoryManager.restoreInventory(sender, itemStacks);

                                        ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>Given the inventory for " + target.getName() + " to " + sender.getName() + ".</red>"));

                                        return 1;
                                    } else {
                                        ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                        return 0;
                                    }
                                } else {
                                    ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with this player.</red>"));

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

                        if(playerData != null) {
                            List<PlayerData.Entry> entryList = playerData.entries();

                            for(PlayerData.Entry entry : entryList) {
                                int id = entryList.indexOf(entry);

                                Date date = new Date(entry.time());

                                Message toolTip = MessageComponentSerializer.message().serialize(FormatUtil.format("<yellow>Time: <red>" + simpleDateFormat.format(date) + "</red>.</yellow>"));

                                suggestionsBuilder.suggest(id, toolTip);
                            }
                        }

                        return suggestionsBuilder.buildFuture();
                    })

                    .executes(ctx -> {
                        Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                        UUID targetUUID = target.getUniqueId();
                        int id = ctx.getArgument("id", int.class);

                        PlayerData playerData = playerDataManager.getPlayerData(targetUUID);
                        if (playerData != null) {
                            if(id < 0 || id >= playerData.entries().size()) {
                                ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

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
                                        Placeholder.parsed("x", String.valueOf(location.x())),
                                        Placeholder.parsed("y", String.valueOf(location.y())),
                                        Placeholder.parsed("z", String.valueOf(location.z())),
                                        Placeholder.parsed("world", location.world()),
                                        Placeholder.parsed("cause", entry.cause()),
                                        Placeholder.parsed("exp", String.valueOf(entry.exp()))
                                );

                                Component timeOfDeath = FormatUtil.format("<yellow>Time of death: " +
                                        "<red><time></red>.</yellow>", placeholders);
                                Component causeOfDeath = FormatUtil.format("<yellow>Cause of death: " +
                                        "<red><cause></red>.</yellow>", placeholders);
                                Component deathLocation = FormatUtil.format("<yellow>Death location: x: " +
                                        "<red><x></red> y: <red><y></red> z: <red><z></red> " +
                                        "in world <red><world></red>.</yellow>", placeholders);

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

                                Component items = FormatUtil.format(stringBuilder.toString());
                                Component exp = FormatUtil.format("<yellow>Exp at death: <red><exp></red>.</yellow>", placeholders);

                                CommandSender sender = ctx.getSource().getSender();

                                sender.sendMessage(timeOfDeath);
                                sender.sendMessage(causeOfDeath);
                                sender.sendMessage(deathLocation);
                                sender.sendMessage(items);
                                sender.sendMessage(exp);

                                return 1;
                            } else {
                                ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with that ID.</red>"));

                                return 0;
                            }
                        } else {
                            ctx.getSource().getSender().sendMessage(FormatUtil.format("<red>There is no data associated with this player.</red>"));

                            return 0;
                        }
                    })
                )
            )
        );

        return builder.build();
    }
}
