package com.github.lukesky19.deathLog.config.player;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ConfigSerializable
public record PlayerData(List<Entry> entries) {
    @ConfigSerializable
    public record Entry(long time, @NotNull String cause, @NotNull Location location, @NotNull List<byte[]> items, int exp) {}

    @ConfigSerializable
    public record Location(@NotNull String world, double x, double y, double z) {}
}
