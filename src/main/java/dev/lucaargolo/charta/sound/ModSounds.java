package dev.lucaargolo.charta.sound;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Charta.MOD_ID);

    public static DeferredHolder<SoundEvent, SoundEvent> CARD_DRAW = SOUNDS.register("card_draw", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_draw")));
    public static DeferredHolder<SoundEvent, SoundEvent> CARD_SHUFFLE = SOUNDS.register("card_shuffle", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_shuffle")));


    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

}
