package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Charta.MOD_ID);
    public static final Map<WoodType, DeferredHolder<Block, CardTableBlock>> CARD_TABLE_MAP = new HashMap<>();
    public static final Map<WoodType, DeferredHolder<Block, GameChairBlock>> GAME_CHAIR_MAP = new HashMap<>();
    public static final Map<WoodType, DeferredHolder<Block, BarStoolBlock>> BAR_STOOL_MAP = new HashMap<>();

    static {
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(f -> {
            ResourceKey<Block> resourceKey = f.getBaseBlock().builtInRegistryHolder().getKey();
            if(resourceKey != null) {
                String woodName = resourceKey.location().withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodName)).findFirst().ifPresent(type -> {
                    Supplier<CardTableBlock> tableSupplier = () -> new CardTableBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                    DeferredHolder<Block, CardTableBlock> tableHolder = BLOCKS.register(woodName + "_card_table", tableSupplier);
                    CARD_TABLE_MAP.put(type, tableHolder);
                    Supplier<GameChairBlock> chairSupplier = () -> new GameChairBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                    DeferredHolder<Block, GameChairBlock> chairHolder = BLOCKS.register(woodName + "_game_chair", chairSupplier);
                    GAME_CHAIR_MAP.put(type, chairHolder);
                    Supplier<BarStoolBlock> stoolSupplier = () -> new BarStoolBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                    DeferredHolder<Block, BarStoolBlock> stoolHolder = BLOCKS.register(woodName + "_bar_stool", stoolSupplier);
                    BAR_STOOL_MAP.put(type, stoolHolder);
                });
            }
        });
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
