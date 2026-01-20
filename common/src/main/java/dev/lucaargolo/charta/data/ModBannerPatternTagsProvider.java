package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.block.ModBannerPatterns;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBannerPatternTagsProvider extends TagsProvider<BannerPattern> {

    public ModBannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.BANNER_PATTERN, provider);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BannerPatternTags.NO_ITEM_REQUIRED).add(
            ModBannerPatterns.SPADES,
            ModBannerPatterns.HEARTS,
            ModBannerPatterns.CLUBS,
            ModBannerPatterns.DIAMONDS
        );
    }

}
