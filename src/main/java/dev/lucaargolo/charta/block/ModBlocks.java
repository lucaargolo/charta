package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Charta.MOD_ID);
    public static final Map<WoodType, RegistryObject<CardTableBlock>> CARD_TABLE_MAP = new HashMap<>();
    public static final Map<WoodType, RegistryObject<GameChairBlock>> GAME_CHAIR_MAP = new HashMap<>();
    public static final Map<WoodType, RegistryObject<BarStoolBlock>> BAR_STOOL_MAP = new HashMap<>();
    public static final Map<WoodType, RegistryObject<BarShelfBlock>> BAR_SHELF_MAP = new HashMap<>();

    public static final RegistryObject<Block> DEALER_TABLE = BLOCKS.register("dealer_table", () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    static {
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(f -> {
            ResourceKey<Block> resourceKey = f.getBaseBlock().builtInRegistryHolder().key();
            if(resourceKey.location().getNamespace().equals("minecraft")) {
                String woodName = resourceKey.location().withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodName)).findFirst().ifPresent(type -> {
                    Supplier<CardTableBlock> tableSupplier = () -> new CardTableBlock(Block.Properties.copy(f.getBaseBlock()));
                    RegistryObject<CardTableBlock> tableHolder = BLOCKS.register(woodName + "_card_table", tableSupplier);
                    CARD_TABLE_MAP.put(type, tableHolder);
                    Supplier<GameChairBlock> chairSupplier = () -> new GameChairBlock(Block.Properties.copy(f.getBaseBlock()));
                    RegistryObject<GameChairBlock> chairHolder = BLOCKS.register(woodName + "_game_chair", chairSupplier);
                    GAME_CHAIR_MAP.put(type, chairHolder);
                    Supplier<BarStoolBlock> stoolSupplier = () -> new BarStoolBlock(Block.Properties.copy(f.getBaseBlock()));
                    RegistryObject<BarStoolBlock> stoolHolder = BLOCKS.register(woodName + "_bar_stool", stoolSupplier);
                    BAR_STOOL_MAP.put(type, stoolHolder);
                    Supplier<BarShelfBlock> shelfSupplier = () -> new BarShelfBlock(Block.Properties.copy(f.getBaseBlock()).noOcclusion());
                    RegistryObject<BarShelfBlock> shelfHolder = BLOCKS.register(woodName + "_bar_shelf", shelfSupplier);
                    BAR_SHELF_MAP.put(type, shelfHolder);
                });
            }
        });
    }

    public static final RegistryObject<BeerGlassBlock> EMPTY_BEER_GLASS = BLOCKS.register("empty_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<BeerGlassBlock> WHEAT_BEER_GLASS = BLOCKS.register("wheat_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<BeerGlassBlock> SORGHUM_BEER_GLASS = BLOCKS.register("sorghum_beer_glass", () -> new BeerGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));

    public static final RegistryObject<WineGlassBlock> EMPTY_WINE_GLASS = BLOCKS.register("empty_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<WineGlassBlock> BERRY_WINE_GLASS = BLOCKS.register("berry_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<WineGlassBlock> CACTUS_WINE_GLASS = BLOCKS.register("cactus_wine_glass", () -> new WineGlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
