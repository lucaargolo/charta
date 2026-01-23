package dev.lucaargolo.charta.common.sound;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final ModRegistry<SoundEvent> REGISTRY = ChartaMod.registry(Registries.SOUND_EVENT);

    public static MinecraftEntry<SoundEvent> CARD_DRAW = REGISTRY.register("card_draw", () -> SoundEvent.createVariableRangeEvent(ChartaMod.id("card_draw")));
    public static MinecraftEntry<SoundEvent> CARD_PLAY = REGISTRY.register("card_play", () -> SoundEvent.createVariableRangeEvent(ChartaMod.id("card_play")));

}
