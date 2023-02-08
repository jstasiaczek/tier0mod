package com.ciap.mc.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class PressRecipe extends SimpleRecipe {

    public PressRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems, int processTime) {
        super(id, output, recipeItems, processTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TypeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TypeSerializer.INSTANCE;
    }

    public static class TypeSerializer implements RecipeSerializer<PressRecipe>, RecipeType<PressRecipe>  {
        public static final TypeSerializer INSTANCE = new TypeSerializer();
        public static final String ID = "press";
        // this is the name given in the json file

        @Override
        public PressRecipe read(Identifier id, JsonObject json) {
            RecipeContent recipeContent = RecipeSerializerHelper.readJson(json);
            return new PressRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public PressRecipe read(Identifier id, PacketByteBuf buf) {
            RecipeContent recipeContent = RecipeSerializerHelper.readBuf(buf);
            return new PressRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public void write(PacketByteBuf buf, PressRecipe recipe) {
            RecipeSerializerHelper.writeBuf(buf, new RecipeContent(recipe.getOutput(), recipe.getIngredients(), recipe.getProcessTime()));
        }
    }
}
