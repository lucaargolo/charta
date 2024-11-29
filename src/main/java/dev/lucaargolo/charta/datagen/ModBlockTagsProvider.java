package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {

    @SuppressWarnings("deprecation")
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, lookupProvider, block -> block.builtInRegistryHolder().key(), Charta.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        List<Block> mineableWithAxe = new ArrayList<>();
        mineableWithAxe.addAll(ModBlocks.CARD_TABLE_MAP.values().stream().map(DeferredHolder::get).toList());
        mineableWithAxe.addAll(ModBlocks.GAME_CHAIR_MAP.values().stream().map(DeferredHolder::get).toList());
        mineableWithAxe.addAll(ModBlocks.BAR_STOOL_MAP.values().stream().map(DeferredHolder::get).toList());
        mineableWithAxe.addAll(ModBlocks.BAR_SHELF_MAP.values().stream().map(DeferredHolder::get).toList());
        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(mineableWithAxe.toArray(new Block[0]));
    }
}
