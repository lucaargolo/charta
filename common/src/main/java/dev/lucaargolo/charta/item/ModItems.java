package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.BeerGlassBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.block.WineGlassBlock;
import dev.lucaargolo.charta.registry.ModItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;

public class ModItems {

    public static final ModItemRegistry REGISTRY = ChartaMod.itemRegistry();

    static {
        ModBlocks.REGISTRY.getEntries().forEach((entry) -> {
            ResourceLocation location = entry.key();
            String path = location.getPath();
            if(!path.contains("empty")) {
                if(path.contains("wine_glass")) {
                    REGISTRY.register(path, () -> new DrinkGlassBlockItem(entry.get(), ModBlocks.EMPTY_WINE_GLASS.get(), new Item.Properties().food(WineGlassBlock.FOOD)));
                }else if(path.contains("beer_glass")) {
                    REGISTRY.register(path, () -> new DrinkGlassBlockItem(entry.get(), ModBlocks.EMPTY_BEER_GLASS.get(), new Item.Properties().food(BeerGlassBlock.FOOD)));
                }else{
                    REGISTRY.register(path, () -> new BlockItem(entry.get(), new Item.Properties()));
                }
            }else{
                REGISTRY.register(path, () -> new BlockItem(entry.get(), new Item.Properties()));
            }
        });
    }

    public static final ModItemRegistry.ItemEntry<DeckItem> DECK = REGISTRY.register("deck", () -> new DeckItem(new Item.Properties().stacksTo(1)));
    public static final ModItemRegistry.ItemEntry<LeadItem> IRON_LEAD = REGISTRY.register("iron_lead", () -> new LeadItem(new Item.Properties()));

}
