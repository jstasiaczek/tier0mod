package com.ciap.mc.screen;

import com.ciap.mc.block.entity.HarvesterBlockEntity;
import com.ciap.mc.screen.slot.HarvesterFuelSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

public class HarvesterScreenHandler extends ScreenHandlerAbstract {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public HarvesterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(10), new ArrayPropertyDelegate(2));
    }

    public HarvesterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlerRegistry.HARVESTER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 10);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new HarvesterFuelSlot( inventory, 9, 133,17));

        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 3; ++l) {
                this.addSlot(new Slot(inventory, l + (i*3), 62 + l * 18, 17 + i * 18));
            }
        }

        addPlayerHotbar(playerInventory);
        addPlayerInventory(playerInventory);
        addProperties(propertyDelegate);
    }

    public int getCokeProgress() {
        int cokeAmount = this.propertyDelegate.get(1);
        int cokeStorageHeight = 61;

        return cokeAmount == 0 ? 0 : (int) (((float)cokeAmount / (float) HarvesterBlockEntity.CHUNK_MAX_COUNT) * cokeStorageHeight);
    }

    public String getCokeText() {
        int oilAmount = this.propertyDelegate.get(1);
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
