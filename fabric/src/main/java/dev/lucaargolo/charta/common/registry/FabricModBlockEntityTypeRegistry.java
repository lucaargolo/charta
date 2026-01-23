package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricModBlockEntityTypeRegistry extends ModBlockEntityTypeRegistry {

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    @Override
    public <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, ModBlockRegistry.BlockEntry<?>... blocks) {
        return register(path, () -> {
            Block[] blockArray = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                blockArray[i] = blocks[i].get();
            }
            return BlockEntityType.Builder.of(factory::apply, blockArray).build();
        });
    }

    @Override
    public <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, Supplier<Block[]> blocks) {
        return register(path, () -> {
            return BlockEntityType.Builder.of(factory::apply, blocks.get()).build();
        });
    }

    @Override
    public <E extends BlockEntityType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends BlockEntityType<?>> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, entry.key(), entry.get()));
    }

}
