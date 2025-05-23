package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBannerPatterns;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBannerPatternTagsProvider extends TagsProvider<BannerPattern> {

    public ModBannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.BANNER_PATTERN, provider, Charta.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BannerPatternTags.NO_ITEM_REQUIRED).add(
            ModBannerPatterns.SPADES.getKey(),
            ModBannerPatterns.HEARTS.getKey(),
            ModBannerPatterns.CLUBS.getKey(),
            ModBannerPatterns.DIAMONDS.getKey()
        );
    }

}
