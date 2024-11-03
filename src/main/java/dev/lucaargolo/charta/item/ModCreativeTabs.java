package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Charta.MOD_ID);
    
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("mod_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Charta"))
            .icon(ModBlocks.CARD_TABLE_MAP.get(WoodType.OAK).get().asItem()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().stream().filter(h -> h != ModItems.DECK).map(DeferredHolder::get).forEach(output::accept);
                Charta.CARD_DECKS.getDecks().forEach((id, deck) -> {
                    ItemStack stack = ModItems.DECK.get().getDefaultInstance();
                    stack.set(ModDataComponentTypes.CARD_DECK.get(), deck);
                    output.accept(stack);
                });
            })
            .build());
    
    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
