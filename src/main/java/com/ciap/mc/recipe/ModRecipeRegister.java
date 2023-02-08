package com.ciap.mc.recipe;

import net.minecraft.util.registry.Registry;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class ModRecipeRegister {
    public static void setup() {
        Registry.register(Registry.RECIPE_TYPE, getIdentifier(ChamberRecipe.TypeSerializer.ID), ChamberRecipe.TypeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, getIdentifier(ChamberRecipe.TypeSerializer.ID), ChamberRecipe.TypeSerializer.INSTANCE);

        Registry.register(Registry.RECIPE_SERIALIZER, getIdentifier(PressRecipe.TypeSerializer.ID), PressRecipe.TypeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, getIdentifier(PressRecipe.TypeSerializer.ID), PressRecipe.TypeSerializer.INSTANCE);

        Registry.register(Registry.RECIPE_SERIALIZER, getIdentifier(CrusherRecipe.TypeSerializer.ID), CrusherRecipe.TypeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, getIdentifier(CrusherRecipe.TypeSerializer.ID), CrusherRecipe.TypeSerializer.INSTANCE);

        Registry.register(Registry.RECIPE_SERIALIZER, getIdentifier(SawmillRecipe.TypeSerializer.ID), SawmillRecipe.TypeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, getIdentifier(SawmillRecipe.TypeSerializer.ID), SawmillRecipe.TypeSerializer.INSTANCE);
    }
}
