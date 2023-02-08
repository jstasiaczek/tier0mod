package com.ciap.mc.rei;

import com.ciap.mc.Tier0Mod;
import com.ciap.mc.block.ModBlockRegistry;
import com.ciap.mc.recipe.ChamberRecipe;
import com.ciap.mc.recipe.CrusherRecipe;
import com.ciap.mc.recipe.PressRecipe;
import com.ciap.mc.recipe.SawmillRecipe;
import com.ciap.mc.rei.display.ChamberDisplay;
import com.ciap.mc.rei.display.CrusherDisplay;
import com.ciap.mc.rei.display.PressDisplay;
import com.ciap.mc.rei.display.SawmillDisplay;
import com.ciap.mc.rei.display.category.ChamberDisplayCategory;
import com.ciap.mc.rei.display.category.CrusherDisplayCategory;
import com.ciap.mc.rei.display.category.PressDisplayCategory;
import com.ciap.mc.rei.display.category.SawmillDisplayCategory;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class Tier0ReiClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<ChamberDisplay> CHAMBER_DISPLAY_CATEGORY_IDENTIFIER = CategoryIdentifier.of(Tier0Mod.MOD_ID, "chamber_display");
    public static final CategoryIdentifier<PressDisplay> PRESS_DISPLAY_CATEGORY_IDENTIFIER = CategoryIdentifier.of(Tier0Mod.MOD_ID, "press_display");
    public static final CategoryIdentifier<CrusherDisplay> CRUSHER_DISPLAY_CATEGORY_IDENTIFIER = CategoryIdentifier.of(Tier0Mod.MOD_ID, "crusher_display");
    public static final CategoryIdentifier<SawmillDisplay> SAWMILL_DISPLAY_CATEGORY_IDENTIFIER = CategoryIdentifier.of(Tier0Mod.MOD_ID, "sawmill_display");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ChamberDisplayCategory());
        registry.add(new PressDisplayCategory());
        registry.add(new CrusherDisplayCategory());
        registry.add(new SawmillDisplayCategory());

        registry.addWorkstations(CHAMBER_DISPLAY_CATEGORY_IDENTIFIER, EntryStacks.of(ModBlockRegistry.CHAMBER_BLOCK));
        registry.addWorkstations(PRESS_DISPLAY_CATEGORY_IDENTIFIER, EntryStacks.of(ModBlockRegistry.PRESS));
        registry.addWorkstations(CRUSHER_DISPLAY_CATEGORY_IDENTIFIER, EntryStacks.of(ModBlockRegistry.CRUSHER));
        registry.addWorkstations(SAWMILL_DISPLAY_CATEGORY_IDENTIFIER, EntryStacks.of(ModBlockRegistry.SAWMILL));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(
                ChamberRecipe.class,
                chamberRecipe -> new ChamberDisplay(getInputs(chamberRecipe.getIngredients()), getOutputs(chamberRecipe.getOutput()))
        );

        registry.registerFiller(
                PressRecipe.class,
                pressRecipe -> new PressDisplay(getInputs(pressRecipe.getIngredients()), getOutputs(pressRecipe.getOutput()))
        );

        registry.registerFiller(
                CrusherRecipe.class,
                crusherRecipe -> new CrusherDisplay(getInputs(crusherRecipe.getIngredients()), getOutputs(crusherRecipe.getOutput()))
        );

        registry.registerFiller(
                SawmillRecipe.class,
                sawmillRecipe -> new SawmillDisplay(getInputs(sawmillRecipe.getIngredients()), getOutputs(sawmillRecipe.getOutput()))
        );
    }

    public static List<EntryIngredient> getInputs(DefaultedList<Ingredient> ingredients) {
        List<EntryIngredient> inputs = new ArrayList<>();
        for (Ingredient element : ingredients) {
            ItemStack item = element.getMatchingStacks().length > 0 ? element.getMatchingStacks()[0] : ItemStack.EMPTY;
            inputs.add(EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, item)));
        }
        return inputs;
    }

    public static List<EntryIngredient> getOutputs(ItemStack result) {
        List<EntryIngredient> output = new ArrayList<>();
        output.add(EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, result)));
        return output;
    }

}
