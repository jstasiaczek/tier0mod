package com.ciap.mc.recipe;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public abstract class SimpleRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final ItemStack output;
    private final DefaultedList<Ingredient> recipeItems;

    private final int processTime;

    public SimpleRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems, int processTime) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.processTime = processTime;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return recipeItems;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if(world.isClient()) { return false; }

        if (inventory.size() != recipeItems.size()) { return false; }

        for (int i = 0; i < recipeItems.size(); i++) {
            if (!recipeItems.get(i).test(inventory.getStack(i))) { return false; }
        }
        return true;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return output.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public int getProcessTime() {
        return processTime;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
