package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class ModBlockTagsProvider extends FabricTagProvider.BlockTagProvider {

    public ModBlockTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        List<Block> mineableWithAxe = new ArrayList<>();
        mineableWithAxe.addAll(ModBlocks.CARD_TABLE_MAP.values());
        mineableWithAxe.addAll(ModBlocks.GAME_CHAIR_MAP.values());
        mineableWithAxe.addAll(ModBlocks.BAR_STOOL_MAP.values());
        mineableWithAxe.addAll(ModBlocks.BAR_SHELF_MAP.values());
        mineableWithAxe.sort(Comparator.comparing(block -> block.builtInRegistryHolder().key().location()));
        tag(BlockTags.MINEABLE_WITH_AXE).addAll(mineableWithAxe.stream().map(block -> block.builtInRegistryHolder().key()).toList());
    }
}
