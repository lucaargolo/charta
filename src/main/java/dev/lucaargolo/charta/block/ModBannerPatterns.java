package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.HashMap;
import java.util.Map;

public class ModBannerPatterns {

    private static final Map<ResourceKey<BannerPattern>, BannerPattern> PATTERNS = new HashMap<>();

    public static final ResourceKey<BannerPattern> SPADES = register("spades");
    public static final ResourceKey<BannerPattern> HEARTS = register("hearts");
    public static final ResourceKey<BannerPattern> CLUBS = register("clubs");
    public static final ResourceKey<BannerPattern> DIAMONDS = register("diamonds");

    private static ResourceKey<BannerPattern> register(String name) {
        ResourceKey<BannerPattern> resourceKey = ResourceKey.create(Registries.BANNER_PATTERN, Charta.id(name));
        BannerPattern pattern = new BannerPattern("block.minecraft.banner."+resourceKey.location().toShortLanguageKey());
        PATTERNS.put(resourceKey, pattern);
        return resourceKey;
    }

//    public static void bootstrap(BootstrapContext<BannerPattern> context) {
//        PATTERNS.forEach(context::register);
//    }


}
