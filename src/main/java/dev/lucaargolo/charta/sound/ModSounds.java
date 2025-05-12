package dev.lucaargolo.charta.sound;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Charta.MOD_ID);

    public static RegistryObject<SoundEvent> CARD_DRAW = SOUNDS.register("card_draw", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_draw")));
    public static RegistryObject<SoundEvent> CARD_PLAY = SOUNDS.register("card_play", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_play")));


    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

}
