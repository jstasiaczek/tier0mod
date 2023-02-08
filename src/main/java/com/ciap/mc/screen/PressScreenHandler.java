package com.ciap.mc.screen;

import com.ciap.mc.block.entity.PressBlockEntity;
import com.ciap.mc.screen.slot.FuelSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

public class PressScreenHandler extends ScreenHandlerAbstract {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public PressScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5), new ArrayPropertyDelegate(5));
    }

    public PressScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlerRegistry.PRESS_SCREEN_HANDLER, syncId);
        checkSize(inventory, 5);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new FuelSlot( inventory, PressBlockEntity.SLOT_FUEL, 7,32));
        this.addSlot(new Slot( inventory, PressBlockEntity.SLOT_INPUT_ITEM, 34,32));
        this.addSlot(new Slot( inventory, PressBlockEntity.SLOT_RESULT, 81,32));
        this.addSlot(new Slot( inventory, PressBlockEntity.SLOT_OIL_INSERT, 130,15));
        this.addSlot(new Slot( inventory, PressBlockEntity.SLOT_OIL_EXTRACT, 130,60));

        addPlayerHotbar(playerInventory);
        addPlayerInventory(playerInventory);
        addProperties(propertyDelegate);
    }

    public boolean hasFuel() {
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledFuelProgress() {
        int fuelProgress = this.propertyDelegate.get(0);
        int maxFuelProgress = this.propertyDelegate.get(1);
        int fuelProgressSize = 14;

        return maxFuelProgress != 0 ? (int)(((float)fuelProgress / (float)maxFuelProgress) * fuelProgressSize) : 0;
    }

    public int getScaledProgress() {
        int progress = this.propertyDelegate.get(2);
        int maxProgress = this.propertyDelegate.get(3);  // Max Progress
        int progressArrowSize = 26; // This is the width in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public boolean isCrafting() {
        return propertyDelegate.get(2) > 0;
    }

    public int getOilProgress() {
        int oilAmount = this.propertyDelegate.get(4);
        int oilStorageHeight = 61;

        return oilAmount == 0 ? 0 : (int) (((float)oilAmount / (float) PressBlockEntity.MAX_OIL) * oilStorageHeight);
    }

    public String getOilText() {
        int oilAmount = this.propertyDelegate.get(4);
        return ""+oilAmount;
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }


    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
