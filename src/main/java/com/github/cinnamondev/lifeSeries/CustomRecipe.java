package com.github.cinnamondev.lifeSeries;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CustomRecipe {
    private final Plugin p;
    protected final NamespacedKey key;
    protected final ConfigurationSection recipeConfig;
    private Component recipeMessage = Component.empty();
    private final Recipe recipe;
    public CustomRecipe(Plugin p, NamespacedKey recipeKey, ConfigurationSection recipeConfig) {
        this.p = p;
        this.key = recipeKey;
        this.recipeConfig = recipeConfig;
        this.recipe = createRecipe();
    }

    private List<Material> getMaterialList() {
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

    protected ShapedRecipe createShapedRecipe() {
        List<Material> ingredientList = getMaterialList();

        // build recipe message
        recipeMessage = Component.text("Recipe for ")
                .append(getItem().displayName())
                .append(Component.text(" is:").appendNewline())
                .append(Component.text(
                        Lists.partition(ingredientList, 3).stream()
                                .map(row -> String.format(
                                        "%-15s%-15s%-15s",
                                        row.stream()
                                                .map(Objects::toString)
                                                .map(str -> !str.equals("null") ? str : "").toArray()
                                ))
                                .collect(Collectors.joining("\n"))
                ));

        // build map of unique characters for each ingredient
        HashMap<Material, Character> ingredientMap = new HashMap<>();
        AtomicInteger charOffset = new AtomicInteger();
        ingredientList.stream().distinct().forEach(ingredient -> {
            char character = (char) (65+charOffset.getAndIncrement());
            if (ingredientMap.containsKey(ingredient)) { return; }
            if (ingredient == null) { character = ' '; }
            ingredientMap.put(ingredient, character);
        });

        StringBuilder shapeBuilder = ingredientList.stream().map(ingredientMap::get) // map material list to shape string
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append);
        // ensure collection is padded if the array is smaller than a standard grid.
        String shape = shapeBuilder.append(" ".repeat(9 - shapeBuilder.length())).toString();

        // set shape
        ShapedRecipe recipe = new ShapedRecipe(key, getItem()).shape( // assign shape
                shape.substring(0,3),
                shape.substring(3,6),
                shape.substring(6,9)
        );
        // assign ingredients
        ingredientMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .forEach(entry -> recipe.setIngredient(entry.getValue(), entry.getKey()));
        return recipe;
    }

    protected ShapelessRecipe createShapelessRecipe() {
        List<Material> ingredientList = getMaterialList();
        ShapelessRecipe recipe = new ShapelessRecipe(key, getItem());

        // build recipe message
        recipeMessage = Component.text("Recipe for ")
                .append(getItem().displayName())
                .append(Component.text(" is:").appendNewline())
                .append(Component.text(
                        ingredientList.stream().map(Object::toString).collect(Collectors.joining(" and "))
                ));

        ingredientList.stream()
                .filter(Objects::nonNull)
                .forEach(recipe::addIngredient);
        return recipe;
    }

    public Recipe createRecipe() {
        if (recipeConfig.getBoolean("shapeless", false)) { // recipe is shaped
            return createShapelessRecipe();
        } else { // recipe is shapeless
            return createShapedRecipe();
        }
    }

    public Recipe getRecipe() { return this.recipe; }

    public Component getRecipeMessage() { return this.recipeMessage; }
    public ItemStack getItem() {
        String materialName = recipeConfig.getString("block", "").toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            p.getLogger().warning("couldnt get block " + materialName );
            return null;
        }
        return ItemStack.of(material, recipeConfig.getInt("quantity", 1));
    }

}
