package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ModBlockEntityTypeRegistry extends MinecraftRegistry<BlockEntityType<?>, MinecraftEntry<? extends BlockEntityType<?>>> {

    public ModBlockEntityTypeRegistry() {
        super(Registries.BLOCK_ENTITY_TYPE);
    }

    @Override
    public Registry<BlockEntityType<?>> getRegistry() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE;
    }

    public abstract <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, ModBlockRegistry.BlockEntry<?>... blocks);

    public abstract <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, Supplier<Block[]> blocks);

    @Override
    public abstract <E extends BlockEntityType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Override
    protected <E extends BlockEntityType<?>> MinecraftEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new MinecraftEntry<>(path, supplier, tags);
    }

}