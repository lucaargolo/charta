package dev.lucaargolo.charta.registry;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.NeoForgeChartaMod;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgeModBlockEntityTypeRegistry extends ModBlockEntityTypeRegistry {

    private final DeferredRegister<BlockEntityType<?>> registry = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChartaMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeChartaMod.getModBus());
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, ModBlockRegistry.BlockEntry<?>... blocks) {
        return register(path, () -> {
            Block[] blockArray = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                blockArray[i] = blocks[i].get();
            }
            return BlockEntityType.Builder.of(factory::apply, blockArray).build(null);
        });
    }

    @Override
    public <B extends BlockEntity> MinecraftEntry<BlockEntityType<B>> register(String path, BiFunction<BlockPos, BlockState, B> factory, Supplier<Block[]> blocks) {
        return register(path, () -> {
            return BlockEntityType.Builder.of(factory::apply, blocks.get()).build(null);
        });
    }

    @Override
    public <E extends BlockEntityType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}
