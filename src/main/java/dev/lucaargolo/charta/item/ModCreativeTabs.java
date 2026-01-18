package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final Map<ResourceLocation, CreativeModeTab> CREATIVE_MODE_TABS = new HashMap<>();
    
    public static final CreativeModeTab ITEMS = register("items", () -> FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.charta.items"))
            .icon(ModBlocks.CARD_TABLE_MAP.get(WoodType.OAK).asItem()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.values().stream().filter(h -> h != ModItems.DECK).forEach(output::accept);
            })
            .build());

    public static final CreativeModeTab DECKS = register("decks", () -> FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.charta.decks"))
            .icon(() -> DeckItem.getDeck(new ArrayList<>(Charta.CARD_DECKS.getDecks().keySet()).getFirst()))
            .displayItems((parameters, output) -> {
                Charta.CARD_DECKS.getDecks().forEach((id, deck) -> {
                    output.accept(DeckItem.getDeck(id));
                });
            })
            .build());


    private static <T extends CreativeModeTab> T register(String id, Supplier<T> creativeTab) {
        T obj = creativeTab.get();
        CREATIVE_MODE_TABS.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        CREATIVE_MODE_TABS.forEach((id, creativeTab) -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, creativeTab));
    }
}
