package dev.lucaargolo.hexedaces;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HexedAces.MOD_ID)
public class HexedAces {

    public static final String MOD_ID = "hexedaces";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HexedAces(IEventBus modEventBus, ModContainer modContainer) {


    }

}
