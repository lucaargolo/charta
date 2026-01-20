package dev.lucaargolo.charta.client.data;

import dev.lucaargolo.furniture.block.ModBlocks;
import dev.lucaargolo.furniture.data.builder.ModBlockLootBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

public class NeoForgeModBlockLootProvider extends BlockLootSubProvider implements ModBlockLootBuilder {

    protected NeoForgeModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        ModBlockLootProvider.generate(this);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return Streams.of(ModBlocks.REGISTRY.getEntries()).map(entry -> (Block) entry.get()).toList();
    }
    @Override
    public void otherWhenSilkTouch(@NotNull Block block, @NotNull Block other) {
        super.otherWhenSilkTouch(block, other);
    }

    @Override
    public void dropWhenSilkTouch(@NotNull Block block) {
        super.dropWhenSilkTouch(block);
    }

    @Override
    public void dropOther(@NotNull Block block, @NotNull ItemLike item) {
        super.dropOther(block, item);
    }

    @Override
    public void dropSelf(@NotNull Block block) {
        super.dropSelf(block);
    }

    @Override
    public void add(@NotNull Block block, @NotNull Function<Block, LootTable.Builder> factory) {
        super.add(block, factory);
    }

    @Override
    public void add(@NotNull Block block, LootTable.@NotNull Builder builder) {
        super.add(block, builder);
    }

}
