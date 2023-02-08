package com.ciap.mc.block.entity;

import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.utils.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;

public abstract class SimpleOilMachine extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {

    public SimpleOilMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static final int OIL_AMOUNT_BUCKET = 1000;
    public static final int OIL_AMOUNT_BOTTLE = OIL_AMOUNT_BUCKET / 4;

    protected int oilAmount = 0;

    public int getMaxOil() {
        return 3000;
    }

    public int getStolOilExtract() {
        return 4;
    }

    public int getStolOilInsert() {
        return 3;
    }

    protected void tickOil() {
        ItemStack stack = this.getStack(getStolOilExtract());
        if (!stack.equals(ItemStack.EMPTY) && stack.getCount() > 0) {
            if (stack.getItem().equals(Items.BUCKET)) {
                tickOilExtract(OIL_AMOUNT_BUCKET, ModItemRegistry.OIL_BUCKET);
            } else if (stack.getItem().equals(Items.GLASS_BOTTLE) && stack.getCount() == 1) {
                tickOilExtract(OIL_AMOUNT_BOTTLE, ModItemRegistry.OIL_BOTTLE);
            }
        }
        stack = this.getStack(getStolOilInsert());
        if (!stack.equals(ItemStack.EMPTY) && stack.getCount() > 0) {
            if (stack.getItem().equals(ModItemRegistry.OIL_BUCKET)) {
                tickOilInsert(OIL_AMOUNT_BUCKET, Items.BUCKET);
            } else if (stack.getItem().equals(ModItemRegistry.OIL_BOTTLE) && stack.getCount() == 1) {
                tickOilInsert(OIL_AMOUNT_BOTTLE, Items.GLASS_BOTTLE);
            }
        }
    }

    protected void tickOilExtract(int oil, Item emptyItem) {
        if (this.oilAmount >= oil) {
            this.oilAmount -= oil;
            this.setStack(getStolOilExtract(), new ItemStack(emptyItem, 1));
        }
    }

    protected void tickOilInsert(int oil, Item result) {
        int freeOilSpace = getMaxOil() - this.oilAmount;

        if (freeOilSpace >= oil) {
            this.oilAmount += oil;
            this.setStack(getStolOilInsert(), new ItemStack(result, 1));
        }
    }
}
