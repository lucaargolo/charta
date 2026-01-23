package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.NeoForgeChartaMod;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeModItemRegistry extends ModItemRegistry {

    private final DeferredRegister.Items registry = DeferredRegister.createItems(ChartaMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeChartaMod.getModBus());
    }

    @Override
    public <E extends Item> ItemEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        ItemEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}