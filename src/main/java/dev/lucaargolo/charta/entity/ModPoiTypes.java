package dev.lucaargolo.charta.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Charta.MOD_ID);

    public static final RegistryObject<PoiType> DEALER = POI_TYPES.register("dealer", () -> new PoiType(blockStates(ModBlocks.DEALER_TABLE.get()), 1, 1));

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
    }

    private static Set<BlockState> blockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

}
