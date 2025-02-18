package com.github.cinnamondev.lifeSeries.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class UtilityComponents {
    private UtilityComponents() {}

    public static Component playerTimeChange(int ticks) {
        return playerTimeChange(ticks/20, TimeUnit.SECONDS);
    }

    public static Component playerTimeChange(int time, TimeUnit timeUnit) {
        char sign = (time >= 0) ? '+' : '-';
        Style style = (time >= 0) ? Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)
                : Style.style(NamedTextColor.RED, TextDecoration.BOLD);
        String timeString = DurationFormatUtils.formatDuration(
                timeUnit.toSeconds(time) * 1000L,
                "'" + sign + "'HH':'mm':'ss",
                true);

        return Component.text(timeString).style(style);
    }

    public static Component playerTime(int ticks, TextColor color) {
        return playerTime(ticks/20, TimeUnit.SECONDS, color);
    }

    public static Component playerTime(int time, TimeUnit timeUnit, TextColor color) {
        String timeString = DurationFormatUtils.formatDuration(
                timeUnit.toSeconds(time) * 1000L,
                "HH':'mm':'ss",
                true);
        return Component.text(timeString).style(Style.style(color, TextDecoration.BOLD));
    }

    public static Component teleportButton(Player teleportTo) {
        return Component.translatable("general.teleport-to-player")
                .style(Style.style(NamedTextColor.GRAY, TextDecoration.BOLD))
                .clickEvent(ClickEvent.runCommand("/minecraft:tp " + teleportTo.getName()));
    }

    public static Component dyeList() {
        Component dyeList = Arrays.stream(DyeColor.values()).map(DyeColor::toString)
                .map(Component::text)
                .reduce(Component.text(""), (acc, text) ->
                        (net.kyori.adventure.text.TextComponent) acc.append(text).appendNewline()
                );

        return Component.text("This command requires a dye color. These are the following options:")
                .appendNewline()
                .append(dyeList);
    }

    public static Component teleportToPlayer(Player teleportTo) {
        return Component.translatable("general.teleport-to-player")
                .style(Style.style(NamedTextColor.GRAY, TextDecoration.BOLD))
                .clickEvent(ClickEvent.runCommand("/minecraft:tp " + teleportTo.getName()));
    }
}
