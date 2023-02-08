package com.ciap.mc.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

public class RecipeContent {
    private ItemStack output;
    private DefaultedList<Ingredient> inputs;
    private int processTime;

    public RecipeContent(ItemStack output, DefaultedList<Ingredient> inputs, int processTime) {
        this.output = output;
        this.inputs = inputs;
        this.processTime = processTime;
    }

    public ItemStack getOutput() {
        return output;
    }

    public DefaultedList<Ingredient> getInputs() {
        return inputs;
    }

    public int getProcessTime() {
        return processTime;
    }
}
