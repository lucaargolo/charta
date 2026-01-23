package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.NeoForgeChartaMod;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeModRegistry<T> extends ModRegistry<T> {

    private final DeferredRegister<T> registry;
    private final Registry<T> minecraftRegistry;

    public NeoForgeModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        this.registry = DeferredRegister.create(registryKey, ChartaMod.MOD_ID);
        if (registryKey.location().getNamespace().equals(ChartaMod.MOD_ID)) {
            this.registry.makeRegistry(builder -> {
                builder.sync(true);
            });
        }
        this.minecraftRegistry = this.registry.getRegistry().get();
    }

    @Override
    public void init() {
        this.registry.register(NeoForgeChartaMod.getModBus());
    }

    @Override
    public Registry<T> getRegistry() {
        return this.minecraftRegistry;
    }

    @Override
    public <E extends T> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}