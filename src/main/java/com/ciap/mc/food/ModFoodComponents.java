package com.ciap.mc.food;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent URANIUM_CARROT = (new FoodComponent.Builder()).hunger(-4).saturationModifier(0).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 500, 1), 1.0F).statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1000, 1), 1.0F).alwaysEdible().build();
}
