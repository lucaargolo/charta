package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.BeerGlassBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.block.WineGlassBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {

    public static final Map<ResourceLocation, Item> ITEMS = new HashMap<>();

    static {
        ModBlocks.BLOCKS.forEach((location, block) -> {
            String path = location.getPath();
            if (!path.contains("empty")) {
                if (path.contains("wine_glass")) {
                    register(location.getPath(), () -> new DrinkGlassBlockItem(block, ModBlocks.EMPTY_WINE_GLASS, new Item.Properties().food(WineGlassBlock.FOOD)));
                } else if (path.contains("beer_glass")) {
                    register(location.getPath(), () -> new DrinkGlassBlockItem(block, ModBlocks.EMPTY_BEER_GLASS, new Item.Properties().food(BeerGlassBlock.FOOD)));
                } else {
                    register(location.getPath(), () -> new BlockItem(block, new Item.Properties()));
                }
            } else {
                register(location.getPath(), () -> new BlockItem(block, new Item.Properties()));
            }
        });
    }

    public static final DeckItem DECK = register("deck", () -> new DeckItem(new Item.Properties().stacksTo(1)));
    public static final LeadItem IRON_LEAD = register("iron_lead", () -> new LeadItem(new Item.Properties()));

    private static <T extends Item> T register(String id, Supplier<T> item) {
        T obj = item.get();
        ITEMS.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        ITEMS.forEach((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
    }


}
