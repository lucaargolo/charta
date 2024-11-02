package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

    static {
        BlockFamilies.getAllFamilies().filter(f -> f.getRecipeGroupPrefix().orElse("").equals("wooden")).forEach(f -> {
            ResourceKey<Block> resourceKey = f.getBaseBlock().builtInRegistryHolder().getKey();
            if(resourceKey != null) {
                String woodName = resourceKey.location().withPath(s -> s.replace("_planks", "")).getPath();
                WoodType.values().filter(t -> t.name().equals(woodName)).findFirst().ifPresent(type -> {
                    Supplier<CardTableBlock> supplier = () -> new CardTableBlock(Block.Properties.ofFullCopy(f.getBaseBlock()).noOcclusion());
                    DeferredHolder<Block, CardTableBlock> holder = BLOCKS.register(woodName + "_card_table", supplier);
                    CARD_TABLE_MAP.put(type, holder);
                });
            }
        });
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
