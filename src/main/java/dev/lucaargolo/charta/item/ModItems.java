package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.BeerGlassBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.block.WineGlassBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Charta.MOD_ID);

    static {
        ModBlocks.BLOCKS.getEntries().forEach(holder -> {
            ResourceKey<Block> resourceKey = holder.getKey();
            if(resourceKey != null) {
                String path = resourceKey.location().getPath();
                if(!path.contains("empty")) {
                    if(path.contains("wine_glass")) {
                        ITEMS.register(resourceKey.location().getPath(), () -> new DrinkGlassBlockItem(holder.get(), ModBlocks.EMPTY_WINE_GLASS.get(), new Item.Properties().food(WineGlassBlock.FOOD)));
                    }else if(path.contains("beer_glass")) {
                        ITEMS.register(resourceKey.location().getPath(), () -> new DrinkGlassBlockItem(holder.get(), ModBlocks.EMPTY_BEER_GLASS.get(), new Item.Properties().food(BeerGlassBlock.FOOD)));
                    }else{
                        ITEMS.register(resourceKey.location().getPath(), () -> new BlockItem(holder.get(), new Item.Properties()));
                    }
                }else{
                    ITEMS.register(resourceKey.location().getPath(), () -> new BlockItem(holder.get(), new Item.Properties()));
                }
            }
        });
    }

    public static final RegistryObject<DeckItem> DECK = ITEMS.register("deck", () -> new DeckItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<LeadItem> IRON_LEAD = ITEMS.register("iron_lead", () -> new LeadItem(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }


}
