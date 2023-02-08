package com.ciap.mc.item;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.List;

// Based on https://github.com/kwpugh/gobber_fabric Teleport Ring

public class TeleportWand extends Item
{
    public TeleportWand(Settings settings)
    {
        super(settings);
    }

    // On right click, use the teleport function or clear location
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack stack = player.getStackInHand(hand);
        BlockPos position = getPosition(stack);
        boolean isSneaking = player.isSneaking();

        if (position == null) {
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }

        if(!isSneaking)
        {
            teleport(player, world, stack);
        } else {
            setPosition(stack, world, null, player);
            player.sendMessage((Text.translatable("item.tier0.teleport_wand.tip8")), true);   // loc cleared
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    // Right-click on a block
    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        Direction direction = context.getSide();
        ItemStack stack = context.getPlayer().getMainHandStack();

        if(getPosition(stack) == null && !player.isSneaking())
        {
            player.sendMessage((Text.translatable("item.tier0.teleport_wand.tip2")), true);   // not set
        }

        if(getPosition(stack) == null && player.isSneaking())
        {
            setPosition(stack, world, pos.offset(direction), player);
            player.sendMessage((Text.translatable("item.tier0.teleport_wand.tip6", getLocationString(stack)).formatted(Formatting.GREEN)), true);   // loc set

            return ActionResult.SUCCESS;
        }

        if(getPosition(stack) != null)
        {
            player.sendMessage((Text.translatable("item.tier0.teleport_wand.tip7", getLocationString(stack)).formatted(Formatting.YELLOW)), true);    // loc already set

            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    // Get stored BlockPos (x, y, z), if it exists
    public static BlockPos getPosition(ItemStack stack)
    {
        NbtCompound tags = stack.getNbt();

        if (stack.hasNbt() && tags.contains("pos"))
        {
            NbtCompound subTags = stack.getOrCreateSubNbt("pos");
            return NbtHelper.toBlockPos(subTags);
        }
        else
        {
            return null;
        }
    }

    // Get stored RegistryKey value, if exists
    public static String getDimension(ItemStack stack)
    {
        NbtCompound tags = stack.getNbt();

        if (stack.hasNbt() && tags.contains("dim"))
        {
            return stack.getNbt().getString("dim");
        }
        else
        {
            return null;
        }
    }

    // Set position and dimension in the NBT
    public static void setPosition(ItemStack stack, World world, BlockPos pos, PlayerEntity player)
    {
        if(world.isClient) return;

        NbtCompound tags;

        if (!stack.hasNbt())
        {
            tags = new NbtCompound();
        }
        else
        {
            tags = stack.getNbt();
        }

        if (pos == null) // not pointing at a block will clear the NBT
        {
            tags.remove("pos");
            tags.remove("dim");
        }
        else
        {
            RegistryKey<World> registryKey = world.getRegistryKey();
            Identifier value = registryKey.getValue();
            tags.put("pos", NbtHelper.fromBlockPos(pos));
            tags.putString("dim", value.toString());
        }

        stack.setNbt(tags);
    }

    public boolean consumeCrystal(PlayerEntity player) {
        if (consumeItem(player, ModItemRegistry.MIXED_PORTAL_CRYSTAL)) {
            return true;
        } else if (consumeItem(player, ModItemRegistry.PORTAL_CRYSTAL)) {
            return true;
        }
        return false;
    }

    private boolean consumeItem(PlayerEntity player, Item itemToRemove) {
        int size = player.getInventory().size();
        for (int i = 0; i < size; i++) {
            ItemStack item = player.getInventory().getStack(i);
            if (item.getItem().equals(itemToRemove)) {
                if (item.getCount() == 1) {
                    player.getInventory().setStack(i, ItemStack.EMPTY);
                } else {
                    player.getInventory().setStack(i, new ItemStack(itemToRemove, item.getCount() - 1));
                }
                return true;
            }
        }
        return false;
    }

    public void teleport(PlayerEntity player, World world, ItemStack stack)
    {
        if(world.isClient) return;

        BlockPos pos = getPosition(stack);
        String dim = getDimension(stack);
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // Build world to test from stored data
        RegistryKey<World> storedKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(dim));
        ServerWorld storedWorld = ((ServerWorld)world).getServer().getWorld(storedKey);

        if(storedWorld == null || pos == null) {
            serverPlayer.sendMessage(Text.translatable("item.tier0.teleport_wand.tip9"),true);
        } else if (!consumeCrystal(player)){
            serverPlayer.sendMessage(Text.translatable("item.tier0.teleport_wand.tip10"),true);
        } else {
            TeleportTarget target = new TeleportTarget(new Vec3d(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F), new Vec3d(0, 0, 0), serverPlayer.getYaw(), serverPlayer.getPitch());
            doTeleport(serverPlayer, storedWorld, target);

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    private void doTeleport(ServerPlayerEntity player, ServerWorld world, TeleportTarget target)
    {
        if(player.world.getRegistryKey().equals(world.getRegistryKey()))
        {
            player.networkHandler.requestTeleport(target.position.getX(), target.position.getY(), target.position.getZ(), target.yaw, target.pitch);
        }
        else
        {
            FabricDimensions.teleport(player, world, target);
        }
    }

    public String getLocationString(ItemStack stack)
    {
        int storedX = stack.getOrCreateSubNbt("pos").getInt("X");
        int storedY = stack.getOrCreateSubNbt("pos").getInt("Y");
        int storedZ = stack.getOrCreateSubNbt("pos").getInt("Z");
        String storedWorld = stack.getNbt().getString("dim");
        String displayInfo = storedWorld  + "  x: " + storedX + " y: " + storedY + " z: " + storedZ;

        return displayInfo;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext)
    {
        tooltip.add(Text.translatable("item.tier0.teleport_wand.tip1").formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("item.tier0.teleport_wand.tip2").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("item.tier0.teleport_wand.tip3").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("item.tier0.teleport_wand.tip4").formatted(Formatting.YELLOW));

        if(getPosition(stack) != null)
        {
            tooltip.add(Text.translatable("item.tier0.teleport_wand.tip5", getLocationString(stack)).formatted(Formatting.RED));
        }
    }
}
