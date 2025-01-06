package dev.lucaargolo.charta.sound;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModSounds {

    public static final Map<ResourceLocation, SoundEvent> SOUNDS = new HashMap<>();

    public static SoundEvent CARD_DRAW = register("card_draw", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_draw")));
    public static SoundEvent CARD_PLAY = register("card_play", () -> SoundEvent.createVariableRangeEvent(Charta.id("card_play")));


    private static <T extends SoundEvent> T register(String id, Supplier<T> container) {
        T obj = container.get();
        SOUNDS.put(Charta.id(id), container.get());
        return obj;
    }

    public static void register() {
        SOUNDS.forEach((id, container) -> Registry.register(BuiltInRegistries.SOUND_EVENT, id, container));
    }

}
