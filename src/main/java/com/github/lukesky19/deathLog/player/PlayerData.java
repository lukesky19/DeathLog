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

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the data for logged deaths.
 * @param version The data version.
 * @param entries A {@link List} of {@link Entry}.
 */
@ConfigSerializable
public record PlayerData(
        int version,
        @NonNull List<Entry> entries) {
    /**
     * This record contains the data for a death.
     * @param time The system time when the death occurred.
     * @param cause The cause of the death.
     * @param location The {@link Location} the death occurred at.
     * @param items The {@link List} of byte arrays for the {@link ItemStack}s the player's inventory contained at the death.
     * @param exp The player's experience at the death.
     */
    @ConfigSerializable
    public record Entry(long time, @NonNull String cause, @NonNull Location location, @NonNull List<byte[]> items, int exp) {}
    /**
     * This record contains the data for the location a death occurred at.
     * @param world The world the death occurred in.
     * @param x The x coordinate of the death.
     * @param y The y coordinate of the death.
     * @param z The z coordinate of the death.
     */
    @ConfigSerializable
    public record Location(@Nullable String world, @Nullable Double x, @Nullable Double y, @Nullable Double z) {}
}
