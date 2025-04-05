package com.github.cinnamondev.lifeSeries.customRecipe;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ConfigRecipe<T extends Recipe> implements CustomRecipe<T> {
    protected final Plugin p;
    protected final ConfigurationSection recipeConfig;
    protected final NamespacedKey recipeKey;
    protected final T recipe;
    private Function<ItemStack, ItemStack> itemModifier = (item) -> item;
    public ConfigRecipe(Plugin p, NamespacedKey recipeKey, ConfigurationSection recipeConfig) {
        this.p = p;
        this.recipeConfig = recipeConfig;
        this.recipeKey = recipeKey;
        this.recipe = createRecipe();
    }
    public ConfigRecipe(Plugin p, NamespacedKey recipeKey, ConfigurationSection recipeConfig, Function<ItemStack, ItemStack> itemModifier) {
        this(p, recipeKey, recipeConfig);
        this.itemModifier = itemModifier;
    }

    protected abstract T createRecipe();
    protected List<Material> getMaterialList() {
        return recipeConfig.getStringList("recipe")
                .stream().map(ingredient -> {
                    if (ingredient.equalsIgnoreCase("EMPTY")) { return null; }
                    Material material = Material.getMaterial(ingredient.toUpperCase());
                    if (material == null) {
                        p.getLogger()
                                .warning("Invalid material. it will be kept as an empty. " + ingredient + ".");
                    }
                    return material;
                })
                .toList();
    }

    @Override
    public ItemStack item() {
        String materialName = recipeConfig.getString("block", "").toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            p.getLogger().warning("couldnt get block " + materialName );
            return null;
        }
        return itemModifier.apply(ItemStack.of(material, recipeConfig.getInt("quantity", 1)));
    }

    @Override
    public NamespacedKey recipeKey() { return this.recipeKey; }

    @Override
    public T recipe() { return recipe; }
}
