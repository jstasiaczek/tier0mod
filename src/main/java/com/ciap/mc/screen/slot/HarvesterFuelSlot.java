package com.ciap.mc.screen.slot;

import com.ciap.mc.item.ModItemRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class HarvesterFuelSlot extends Slot {
    public HarvesterFuelSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return isCokeChunks(stack);
    }

    private boolean isCokeChunks(ItemStack stack) {
        return stack.getItem().equals(ModItemRegistry.COAL_COKE_CHUNKS);
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return super.getMaxItemCount(stack);
    }

}