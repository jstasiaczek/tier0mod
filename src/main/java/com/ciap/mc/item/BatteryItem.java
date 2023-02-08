package com.ciap.mc.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BatteryItem extends Item {
    public BatteryItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return super.getItemBarStep(stack);
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        stack.setDamage(2000);
        super.onCraft(stack, world, player);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.tier0.green_lantern.tooltip1", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
    }
}
