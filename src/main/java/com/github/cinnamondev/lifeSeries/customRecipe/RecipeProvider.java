package com.github.cinnamondev.lifeSeries.customRecipe;

import org.bukkit.inventory.Recipe;

public interface RecipeProvider<T extends Recipe> {
    T recipe();
}
