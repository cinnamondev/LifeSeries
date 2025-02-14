package com.github.cinnamondev.lifeSeries.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class UtilityComponents {
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
}
