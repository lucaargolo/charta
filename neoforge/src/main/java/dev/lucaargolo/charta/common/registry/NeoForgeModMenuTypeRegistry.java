package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.NeoForgeChartaMod;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgeModMenuTypeRegistry extends ModMenuTypeRegistry {

    private final DeferredRegister<MenuType<?>> registry = DeferredRegister.create(Registries.MENU, ChartaMod.MOD_ID);

    @Override
    public void init() {
        this.registry.register(NeoForgeChartaMod.getModBus());
    }

    @Override
    public <M extends AbstractContainerMenu, D> AdvancedMenuTypeEntry<M, D> register(String path, TriFunction<Integer, Inventory, D, M> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec, TagKey<?>... tags) {
        IContainerFactory<M> advancedFactory = (windowId, inv, data) -> factory.apply(windowId, inv, streamCodec.decode(data));
        Supplier<MenuType<M>> supplier = () -> new MenuType<>(advancedFactory, FeatureFlags.VANILLA_SET);
        AdvancedMenuTypeEntry<M, D> entry = new AdvancedMenuTypeEntry<>(path, this.registry.register(path, supplier), streamCodec, tags);
        entries.put(path, entry);
        return entry;
    }

    @Override
    public <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory) {
        return register(path, () -> new MenuType<>(factory::apply, FeatureFlags.VANILLA_SET));
    }

    @Override
    public <E extends MenuType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, this.registry.register(path, supplier), tags);
        entries.put(path, entry);
        return entry;
    }

}
