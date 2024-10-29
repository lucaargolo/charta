package dev.lucaargolo.hexedaces.block;

import dev.lucaargolo.hexedaces.HexedAces;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, HexedAces.MOD_ID);

    public static final DeferredHolder<Block, CardTableBlock> CARD_TABLE = BLOCKS.register("card_table", () -> new CardTableBlock(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS)));


    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }


}
