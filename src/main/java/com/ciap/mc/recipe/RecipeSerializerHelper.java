package com.ciap.mc.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class RecipeSerializerHelper {

    public static RecipeContent readJson(JsonObject json) {
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "output"));

        JsonArray ingredients = JsonHelper.getArray(json, "ingredients");
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(ingredients.size(), Ingredient.EMPTY);

        for (int i = 0; i < inputs.size(); i++) {
            inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
        }

        int processTime = json.get("processTime").getAsInt();

        return new RecipeContent(output, inputs, processTime);
    }

    public static RecipeContent readBuf(PacketByteBuf buf) {
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(buf.readInt(), Ingredient.EMPTY);

        for (int i = 0; i < inputs.size(); i++) {
            inputs.set(i, Ingredient.fromPacket(buf));
        }

        ItemStack output = buf.readItemStack();
        int processTime = buf.readInt();
        return new RecipeContent(output, inputs, processTime);
    }

    public static void writeBuf(PacketByteBuf buf, RecipeContent recipe) {
        buf.writeInt(recipe.getInputs().size());
        for (Ingredient ing : recipe.getInputs()) {
            ing.write(buf);
        }
        buf.writeItemStack(recipe.getOutput());
        buf.writeInt(recipe.getProcessTime());
    }
}
