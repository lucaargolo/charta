package dev.lucaargolo.charta.common.item;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.ModBlocks;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.ArrayList;


public class ModCreativeTabs {

    public static final ModRegistry<CreativeModeTab> REGISTRY = ChartaMod.registry(Registries.CREATIVE_MODE_TAB);
    
    public static final MinecraftEntry<CreativeModeTab> ITEMS = REGISTRY.register("items", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.charta.items"))
            .icon(ModBlocks.CARD_TABLE_MAP.get(WoodType.OAK).get().asItem()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                ModItems.REGISTRY.getEntries().stream().filter(h -> h != ModItems.DECK).map(MinecraftEntry::get).forEach(output::accept);
            })
            .build());

    public static final MinecraftEntry<CreativeModeTab> DECKS = REGISTRY.register("decks", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.charta.decks"))
            .icon(() -> DeckItem.getDeck(new ArrayList<>(ChartaMod.CARD_DECKS.getDecks().keySet()).getFirst()))
            .displayItems((parameters, output) -> {
                ChartaMod.CARD_DECKS.getDecks().forEach((id, deck) -> {
                    output.accept(DeckItem.getDeck(id));
                });
            })
            .build());

}
