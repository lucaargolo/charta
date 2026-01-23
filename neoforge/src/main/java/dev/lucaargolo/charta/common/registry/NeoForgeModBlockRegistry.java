package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.NeoForgeChartaMod;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeModBlockRegistry extends ModBlockRegistry {

    private final DeferredRegister.Blocks registry = DeferredRegister.createBlocks(ChartaMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeChartaMod.getModBus());
    }

    @Override
    public <E extends Block> BlockEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        BlockEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}