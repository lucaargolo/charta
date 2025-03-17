package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class ModBlocks {

    public static final Map<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static final Map<WoodType, CardTableBlock> CARD_TABLE_MAP = new HashMap<>();
    public static final Map<WoodType, GameChairBlock> GAME_CHAIR_MAP = new HashMap<>();
    public static final Map<WoodType, BarStoolBlock> BAR_STOOL_MAP = new HashMap<>();
    public static final Map<WoodType, BarShelfBlock> BAR_SHELF_MAP = new HashMap<>();

    public static final Block DEALER_TABLE = register("dealer_table", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    static {
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(f -> {
            Holder.Reference<Block> reference = f.getBaseBlock().builtInRegistryHolder();
            if(reference.isBound()) {
                ResourceKey<Block> resourceKey = reference.key();
                if(resourceKey.location().getNamespace().equals("minecraft")) {
                    String woodName = resourceKey.location().withPath(s -> s.replace("_planks", "")).getPath();
                    WoodType.values().filter(t -> t.name().equals(woodName)).findFirst().ifPresent(type -> {
                        Supplier<CardTableBlock> tableSupplier = () -> new CardTableBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                        CardTableBlock tableHolder = register(woodName + "_card_table", tableSupplier);
                        CARD_TABLE_MAP.put(type, tableHolder);
                        Supplier<GameChairBlock> chairSupplier = () -> new GameChairBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                        GameChairBlock chairHolder = register(woodName + "_game_chair", chairSupplier);
                        GAME_CHAIR_MAP.put(type, chairHolder);
                        Supplier<BarStoolBlock> stoolSupplier = () -> new BarStoolBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                        BarStoolBlock stoolHolder = register(woodName + "_bar_stool", stoolSupplier);
                        BAR_STOOL_MAP.put(type, stoolHolder);
                        Supplier<BarShelfBlock> shelfSupplier = () -> new BarShelfBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                        BarShelfBlock shelfHolder = register(woodName + "_bar_shelf", shelfSupplier);
                        BAR_SHELF_MAP.put(type, shelfHolder);
                    });
                }
            }
        });
    }

    public static final BeerGlassBlock EMPTY_BEER_GLASS = register("empty_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final BeerGlassBlock WHEAT_BEER_GLASS = register("wheat_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final BeerGlassBlock SORGHUM_BEER_GLASS = register("sorghum_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));

    public static final WineGlassBlock EMPTY_WINE_GLASS = register("empty_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final WineGlassBlock BERRY_WINE_GLASS = register("berry_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final WineGlassBlock CACTUS_WINE_GLASS = register("cactus_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));

    private static <T extends Block> T register(String id, Supplier<T> block) {
        T obj = block.get();
        BLOCKS.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        BLOCKS.forEach((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
    }

}
