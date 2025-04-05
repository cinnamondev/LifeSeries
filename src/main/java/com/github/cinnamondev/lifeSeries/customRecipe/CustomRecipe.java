package com.github.cinnamondev.lifeSeries.customRecipe;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;


public interface CustomRecipe<T extends Recipe> extends RecipeProvider<T> {
    NamespacedKey recipeKey();
    ItemStack item();
    Component explainRecipe();
}
