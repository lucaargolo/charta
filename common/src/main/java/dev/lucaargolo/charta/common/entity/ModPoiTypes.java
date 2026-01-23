package dev.lucaargolo.charta.common.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.ModBlocks;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class ModPoiTypes {

    public static final ModRegistry<PoiType> REGISTRY = ChartaMod.registry(Registries.POINT_OF_INTEREST_TYPE);

    public static final MinecraftEntry<PoiType> DEALER = REGISTRY.register("dealer", () -> new PoiType(blockStates(ModBlocks.DEALER_TABLE.get()), 1, 1));

    private static Set<BlockState> blockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

}
