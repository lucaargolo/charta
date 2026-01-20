package dev.lucaargolo.charta.registry;

import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ModMenuTypeRegistry extends MinecraftRegistry<MenuType<?>, MinecraftEntry<? extends MenuType<?>>> {

    public ModMenuTypeRegistry() {
        super(Registries.MENU);
    }

    public abstract <M extends AbstractContainerMenu, D> AdvancedMenuTypeEntry<M, D> register(String path, TriFunction<Integer, Inventory, D, M> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec, TagKey<?>... tags);

    public abstract <M extends AbstractContainerMenu> MinecraftEntry<MenuType<M>> register(String path, BiFunction<Integer, Inventory, M> factory);

    @Override
    protected <E extends MenuType<?>> MinecraftEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new MinecraftEntry<>(id++, path, supplier, tags);
    }

    public static class AdvancedMenuTypeEntry<M extends AbstractContainerMenu, D> extends MinecraftEntry<MenuType<M>> {

        private final StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec;

        public AdvancedMenuTypeEntry(int localId, String path, Supplier<MenuType<M>> supplier, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec, TagKey<?>... tags) {
            super(localId, path, supplier, tags);
            this.streamCodec = streamCodec;
        }

        public StreamCodec<? super RegistryFriendlyByteBuf, D> getStreamCodec() {
            return this.streamCodec;
        }

    }

}