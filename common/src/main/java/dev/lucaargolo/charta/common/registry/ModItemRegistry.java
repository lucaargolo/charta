package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftRegistry;
import dev.lucaargolo.charta.common.utils.ColorProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ModItemRegistry extends MinecraftRegistry<Item, ModItemRegistry.ItemEntry<? extends Item>> {

    public ModItemRegistry() {
        super(Registries.ITEM);
    }

    @Override
    public Registry<Item> getRegistry() {
        return BuiltInRegistries.ITEM;
    }

    @Override
    public abstract <E extends Item> ItemEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Override
    protected <E extends Item> ItemEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new ItemEntry<>(path, supplier, tags);
    }

    public static class ItemEntry<E extends Item> extends MinecraftEntry<E> {

        @Nullable
        private ColorProvider.Item tintColor = null;

        protected ItemEntry(String path, Supplier<E> supplier, TagKey<?>... tags) {
            super( path, supplier, tags);
        }

        public @Nullable ColorProvider.Item getTintColor() {
            return tintColor;
        }

        public ItemEntry<E> withTintColor(ColorProvider.Item itemColor) {
            this.tintColor = itemColor;
            return this;
        }

        public ItemEntry<E> withTintColor(ColorProvider.Block blockColor) {
            this.tintColor = (stack, tintIndex) -> {
                Item item = stack.getItem();
                if(item instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    return blockColor.getColor(block.defaultBlockState(), null, null, tintIndex);
                }else{
                    return 0xFFFFFF;
                }
            };
            return this;
        }

    }

}