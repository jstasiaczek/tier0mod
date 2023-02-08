package com.ciap.mc.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class CrusherRecipe extends SimpleRecipe {

    public CrusherRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems, int processTime) {
        super(id, output, recipeItems, processTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CrusherRecipe.TypeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return CrusherRecipe.TypeSerializer.INSTANCE;
    }

    public static class TypeSerializer implements RecipeSerializer<CrusherRecipe>, RecipeType<CrusherRecipe> {
        public static final CrusherRecipe.TypeSerializer INSTANCE = new CrusherRecipe.TypeSerializer();
        public static final String ID = "crusher";
        // this is the name given in the json file

        @Override
        public CrusherRecipe read(Identifier id, JsonObject json) {
            RecipeContent recipeContent = RecipeSerializerHelper.readJson(json);
            return new CrusherRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public CrusherRecipe read(Identifier id, PacketByteBuf buf) {
            RecipeContent recipeContent = RecipeSerializerHelper.readBuf(buf);
            return new CrusherRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public void write(PacketByteBuf buf, CrusherRecipe recipe) {
            RecipeSerializerHelper.writeBuf(buf, new RecipeContent(recipe.getOutput(), recipe.getIngredients(), recipe.getProcessTime()));
        }
    }
}
