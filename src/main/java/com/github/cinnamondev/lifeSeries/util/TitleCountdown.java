package com.github.cinnamondev.lifeSeries.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TitleCountdown {
    ///  secondsToShow: number of """seconds""" to display counting down every realInterval
    ///  realinterval: delay in ticks between each seconds to show. this is kept seperate so you can tease out the delay
    public static void lifeCountdown(Plugin p, Audience target, Sound tickSound, int delay, int interval, Runnable onCompletion) {
        showSequencedTitles(
                p,
                target,
                List.of(
                        Title.title(
                                Component.text("3")
                                        .style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)),
                                Component.empty()
                        ),
                        Title.title(
                                Component.text("2")
                                        .style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)),
                                Component.empty()
                        ),
                        Title.title(
                                Component.text("1...")
                                        .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)),
                                Component.empty()
                        )
                ),
                tickSound,
                delay,
                interval,
                onCompletion
        );
    }
    public static void showSequencedTitles(Plugin p, Audience audience, List<Title> titles, List<Sound> sounds, int delay, int interval, Runnable onAllTitlesShown) {
        int messageDelay = Math.max(delay,1); // ge1
        var soundIterator = sounds.iterator();
        for (Title title : titles) {
            p.getServer().getScheduler().scheduleSyncDelayedTask(p, () -> {
                audience.showTitle(title);
                if (soundIterator.hasNext()) { audience.playSound(soundIterator.next()); }
            }, delay);
            messageDelay += interval;
        }
        p.getServer().getScheduler().scheduleSyncDelayedTask(p, onAllTitlesShown, delay);
    }
    public static void showSequencedTitles(Plugin p, Audience audience, List<Title> titles, @Nullable Sound sound, int delay, int interval, Runnable onAllTitlesShown) {
        List<Sound> sounds;
        if (sound == null) { sounds = Collections.emptyList(); } else{
            sounds = Collections.nCopies(titles.size(), sound);
        }
        showSequencedTitles(p, audience, titles, sounds, delay, interval, onAllTitlesShown);
    }
}
