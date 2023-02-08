package com.ciap.mc.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class SawmillRecipe extends SimpleRecipe {

    public SawmillRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems, int processTime) {
        super(id, output, recipeItems, processTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SawmillRecipe.TypeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return SawmillRecipe.TypeSerializer.INSTANCE;
    }

    public static class TypeSerializer implements RecipeSerializer<SawmillRecipe>, RecipeType<SawmillRecipe>  {
        public static final SawmillRecipe.TypeSerializer INSTANCE = new SawmillRecipe.TypeSerializer();
        public static final String ID = "sawmill";
        // this is the name given in the json file

        @Override
        public SawmillRecipe read(Identifier id, JsonObject json) {
            RecipeContent recipeContent = RecipeSerializerHelper.readJson(json);
            return new SawmillRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public SawmillRecipe read(Identifier id, PacketByteBuf buf) {
            RecipeContent recipeContent = RecipeSerializerHelper.readBuf(buf);
            return new SawmillRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public void write(PacketByteBuf buf, SawmillRecipe recipe) {
            RecipeSerializerHelper.writeBuf(buf, new RecipeContent(recipe.getOutput(), recipe.getIngredients(), recipe.getProcessTime()));
        }
    }
}
