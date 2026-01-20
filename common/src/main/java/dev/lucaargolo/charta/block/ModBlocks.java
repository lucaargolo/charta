package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.registry.ModBlockRegistry;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class ModBlocks {

    public static final ModBlockRegistry REGISTRY = ChartaMod.blockRegistry();
    public static final Map<WoodType, ModBlockRegistry.BlockEntry<CardTableBlock>> CARD_TABLE_MAP = new HashMap<>();
    public static final Map<WoodType, ModBlockRegistry.BlockEntry<GameChairBlock>> GAME_CHAIR_MAP = new HashMap<>();
    public static final Map<WoodType, ModBlockRegistry.BlockEntry<BarStoolBlock>> BAR_STOOL_MAP = new HashMap<>();
    public static final Map<WoodType, ModBlockRegistry.BlockEntry<BarShelfBlock>> BAR_SHELF_MAP;

    public static final ModBlockRegistry.BlockEntry<Block> DEALER_TABLE = REGISTRY.register("dealer_table", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    static {
        BAR_SHELF_MAP = new HashMap<>();
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(f -> {
            ResourceKey<Block> resourceKey = f.getBaseBlock().builtInRegistryHolder().key();
            if (resourceKey.location().getNamespace().equals("minecraft")) {
                String woodName = resourceKey.location().withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodName)).findFirst().ifPresent(type -> {
                    Supplier<CardTableBlock> tableSupplier = () -> new CardTableBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                    ModBlockRegistry.BlockEntry<CardTableBlock> tableHolder = REGISTRY.register(woodName + "_card_table", tableSupplier);
                    CARD_TABLE_MAP.put(type, tableHolder);
                    Supplier<GameChairBlock> chairSupplier = () -> new GameChairBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                    ModBlockRegistry.BlockEntry<GameChairBlock> chairHolder = REGISTRY.register(woodName + "_game_chair", chairSupplier);
                    GAME_CHAIR_MAP.put(type, chairHolder);
                    Supplier<BarStoolBlock> stoolSupplier = () -> new BarStoolBlock(Block.Properties.ofFullCopy(f.getBaseBlock()));
                    ModBlockRegistry.BlockEntry<BarStoolBlock> stoolHolder = REGISTRY.register(woodName + "_bar_stool", stoolSupplier);
                    BAR_STOOL_MAP.put(type, stoolHolder);
                    Supplier<BarShelfBlock> shelfSupplier = () -> new BarShelfBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                    ModBlockRegistry.BlockEntry<BarShelfBlock> shelfHolder = REGISTRY.register(woodName + "_bar_shelf", shelfSupplier);
                    BAR_SHELF_MAP.put(type, shelfHolder);
                });
            }
        });
    }

    public static final ModBlockRegistry.BlockEntry<BeerGlassBlock> EMPTY_BEER_GLASS = REGISTRY.register("empty_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final ModBlockRegistry.BlockEntry<BeerGlassBlock> WHEAT_BEER_GLASS = REGISTRY.register("wheat_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final ModBlockRegistry.BlockEntry<BeerGlassBlock> SORGHUM_BEER_GLASS = REGISTRY.register("sorghum_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));

    public static final ModBlockRegistry.BlockEntry<WineGlassBlock> EMPTY_WINE_GLASS = REGISTRY.register("empty_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final ModBlockRegistry.BlockEntry<WineGlassBlock> BERRY_WINE_GLASS = REGISTRY.register("berry_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final ModBlockRegistry.BlockEntry<WineGlassBlock> CACTUS_WINE_GLASS = REGISTRY.register("cactus_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));

}
