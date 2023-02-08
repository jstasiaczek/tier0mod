package com.ciap.mc.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ChamberRecipe extends SimpleRecipe {

    public ChamberRecipe(Identifier id, ItemStack output, DefaultedList<Ingredient> recipeItems, int processTime) {
        super(id, output, recipeItems, processTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ChamberRecipe.TypeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ChamberRecipe.TypeSerializer.INSTANCE;
    }

    public static class TypeSerializer implements RecipeSerializer<ChamberRecipe>, RecipeType<ChamberRecipe>  {
        public static final ChamberRecipe.TypeSerializer INSTANCE = new ChamberRecipe.TypeSerializer();
        public static final String ID = "chamber";
        // this is the name given in the json file

        @Override
        public ChamberRecipe read(Identifier id, JsonObject json) {
            RecipeContent recipeContent = RecipeSerializerHelper.readJson(json);
            return new ChamberRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public ChamberRecipe read(Identifier id, PacketByteBuf buf) {
            RecipeContent recipeContent = RecipeSerializerHelper.readBuf(buf);
            return new ChamberRecipe(id, recipeContent.getOutput(), recipeContent.getInputs(), recipeContent.getProcessTime());
        }

        @Override
        public void write(PacketByteBuf buf, ChamberRecipe recipe) {
            RecipeSerializerHelper.writeBuf(buf, new RecipeContent(recipe.getOutput(), recipe.getIngredients(), recipe.getProcessTime()));
        }
    }
}
