package dev.lucaargolo.charta.registry;

import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricModMenuTypeRegistry extends ModMenuTypeRegistry {

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    @Override
    public <M extends AbstractContainerMenu, D> AdvancedMenuTypeEntry<M, D> register(String path, TriFunction<Integer, Inventory, D, M> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec, TagKey<?>... tags) {
        AdvancedMenuTypeEntry<M, D> entry = new AdvancedMenuTypeEntry<>(id++, path, () -> new ExtendedScreenHandlerType<>(factory::apply, streamCodec), streamCodec, tags);
        entries.put(path, entry);
        return entry;
    }

    @Override
    public <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory) {
        return register(path, () -> new MenuType<>(factory::apply, FeatureFlags.VANILLA_SET));
    }

    @Override
    public <E extends MenuType<?>> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends MenuType<?>> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(BuiltInRegistries.MENU, entry.key(), entry.get()));
    }

}
