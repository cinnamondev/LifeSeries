package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.List;

public class GameMasterDialog {
    private final LifeSeries p;
    private final Game g;
    private final Dialog dialog;

    public GameMasterDialog(LifeSeries p, Game g) {
        this.p = p;
        this.g = g;

        this.dialog =Dialog.create(b -> b.empty()
                .type(DialogType.multiAction(
                        List.of(
                                ActionButton.builder(Component.text("game"))
                                        .action(DialogAction.staticAction(ClickEvent.showDialog(g.getDialog()))).build()
                        ),
                        ActionButton.builder(Component.text("")).build(),
                        2
                ))
                .base(DialogBase.builder(Component.text("Gamemaster Menu")).body(List.of(
                        DialogBody.item(ItemStack.of(Material.HEART_POTTERY_SHERD), null, false, false, 64,64),
                        DialogBody.plainMessage(Component.text("Choose a following option..."))
                )).build())
        );
    }


    public Dialog dialog() { return this.dialog; }
}
