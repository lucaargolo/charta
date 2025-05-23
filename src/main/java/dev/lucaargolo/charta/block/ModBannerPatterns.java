package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBannerPatterns {

    private static final DeferredRegister<BannerPattern> PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, Charta.MOD_ID);

    public static final RegistryObject<BannerPattern> SPADES = PATTERNS.register("spades", () -> new BannerPattern("charta:spades"));
    public static final RegistryObject<BannerPattern> HEARTS = PATTERNS.register("hearts", () -> new BannerPattern("charta:hearts"));
    public static final RegistryObject<BannerPattern> CLUBS = PATTERNS.register("clubs", () -> new BannerPattern("charta:clubs"));
    public static final RegistryObject<BannerPattern> DIAMONDS = PATTERNS.register("diamonds", () -> new BannerPattern("charta:diamonds"));

    public static void register(IEventBus bus) {
        PATTERNS.register(bus);
    }


}
