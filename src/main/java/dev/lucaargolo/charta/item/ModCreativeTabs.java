package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;


public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Charta.MOD_ID);
    
    public static final RegistryObject<CreativeModeTab> ITEMS = CREATIVE_MODE_TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.charta.items"))
            .icon(ModBlocks.CARD_TABLE_MAP.get(WoodType.OAK).get().asItem()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().stream().filter(h -> h.get() != ModItems.DECK.get()).map(RegistryObject::get).forEach(output::accept);
            })
            .build());

    public static final RegistryObject<CreativeModeTab> DECKS = CREATIVE_MODE_TABS.register("decks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.charta.decks"))
            .icon(() -> CardDeckItem.getDeck(new ArrayList<>(Charta.CARD_DECKS.getDecks().keySet()).get(0)))
            .displayItems((parameters, output) -> {
                Charta.CARD_DECKS.getDecks().forEach((id, deck) -> {
                    output.accept(CardDeckItem.getDeck(id));
                });
            })
            .build());
    
    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
