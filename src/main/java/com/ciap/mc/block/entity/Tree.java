package com.ciap.mc.block.entity;

import com.ciap.mc.utils.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class Tree {
    private Set<BlockPos> visited = new HashSet<>();
    private List<BlockPos> logs = new LinkedList<>();
    private List<BlockPos> leaves = new LinkedList<>();

    public static boolean isLog(World world, BlockPos pos) {
        return TagUtils.containsBlock(world.getBlockState(pos).getBlock(), BlockTags.LOGS);
    }
    static boolean isLeaves(World world, BlockPos pos) {
        return TagUtils.containsBlock(world.getBlockState(pos).getBlock(), BlockTags.LEAVES);
    }

    public int countLogs() {
        return logs.size();
    }

    private void reset() {
        logs.clear();
        visited.clear();
    }

    public void findTree(World world, BlockPos pos) {
        reset();
        findLogs(world, pos);
        visited.clear();
        findLeaves(world);
    }

    public List<ItemStack> cutTree(World world) {
        ArrayList<ItemStack> drop = new ArrayList<>();
        for (BlockPos pos : leaves) {
            BlockState blockState = world.getBlockState(pos);
            List<ItemStack> droppedStacks = Block.getDroppedStacks(blockState, (ServerWorld) world, pos, null);
            drop = compactInventory(drop, droppedStacks);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
        for (BlockPos pos : logs) {
            BlockState blockState = world.getBlockState(pos);
            List<ItemStack> droppedStacks = Block.getDroppedStacks(blockState, (ServerWorld) world, pos, null);
            drop = compactInventory(drop, droppedStacks);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
        return drop;
    }

    private ArrayList<ItemStack> compactInventory(ArrayList<ItemStack> drop, List<ItemStack> dropped) {
        for (ItemStack stack : dropped) {
            boolean stackFilled = false;
            for (int i = 0; i < drop.size(); i++) {
                ItemStack item = drop.get(i);
                if (item.getItem().equals(stack.getItem()) && item.getCount() < item.getMaxCount()) {
                    stackFilled = true;
                    int leftCount = item.getMaxCount() - (item.getCount() + stack.getCount());
                    if (leftCount >= 0) {
                        drop.set(i, new ItemStack(item.getItem(), item.getMaxCount() - leftCount));
                    } else {
                        drop.set(i, new ItemStack(item.getItem(), item.getMaxCount()));
                        drop.add(new ItemStack(item.getItem(), Math.abs(leftCount)));
                    }
                    break;
                }
            }
            if (!stackFilled) {
                drop.add(new ItemStack(stack.getItem(), stack.getCount()));
            }
        }
        return drop;
    }

    public static int size(Iterable data) {

        if (data instanceof Collection) {
            return ((Collection<?>) data).size();
        }
        int counter = 0;
        for (Object i : data) {
            counter++;
        }
        return counter;
    }

    private void findLogs(World world, BlockPos currentLog) {
        while (!visited.contains(currentLog)) {
            visited.add(currentLog.toImmutable());
            if (!isLog(world, currentLog)) {
                continue;
            }
            logs.add(currentLog);

            BlockPos start = currentLog.add(-1, 0, -1);
            BlockPos end = currentLog.add(1, 1, 1);
            Iterable<BlockPos> toScan = BlockPos.iterate(start, end);
            for (BlockPos pos : toScan) {
                findLogs(world, pos.toImmutable());
            }

            currentLog = currentLog.add(0, 1, 0);
        }
    }

    private void findLeaves(World world) {
        for (BlockPos log : logs) {
            BlockPos start = log.add(-3, 0, -3);
            BlockPos end = log.add(3, 3, 3);
            Iterable<BlockPos> toScan = BlockPos.iterate(start, end);
            for (BlockPos pos : toScan) {
                BlockPos leaf = pos.toImmutable();
                if (visited.contains(leaf)) {
                    continue;
                }
                visited.add(leaf);

                if (!isLeaves(world, leaf)) {
                    continue;
                }
                leaves.add(leaf);
            }
        }
    }
}
