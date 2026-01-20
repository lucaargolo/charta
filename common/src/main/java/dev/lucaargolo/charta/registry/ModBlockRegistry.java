package dev.lucaargolo.charta.registry;

import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftRegistry;
import dev.lucaargolo.charta.utils.ColorProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ModBlockRegistry extends MinecraftRegistry<Block, ModBlockRegistry.BlockEntry<? extends Block>> {

    public ModBlockRegistry() {
        super(Registries.BLOCK);
    }

    @Override
    public abstract <E extends Block> BlockEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Override
    protected <E extends Block> BlockEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new BlockEntry<>(id++, path, supplier, tags);
    }

    public static class BlockEntry<E extends Block> extends MinecraftEntry<E> {

        @Nullable
        private ColorProvider.Block tintColor = null;

        protected BlockEntry(int localId, String path, Supplier<E> supplier, TagKey<?>... tags) {
            super(localId, path, supplier, tags);
        }

        public @Nullable ColorProvider.Block getTintColor() {
            return tintColor;
        }

        public BlockEntry<E> withTintColor(ColorProvider.Block blockColor) {
            this.tintColor = blockColor;
            return this;
        }

    }

}