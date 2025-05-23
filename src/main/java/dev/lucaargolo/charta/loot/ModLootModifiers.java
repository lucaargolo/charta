package dev.lucaargolo.charta.loot;

import com.mojang.serialization.Codec;
import dev.lucaargolo.charta.Charta;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Charta.MOD_ID);

    public static final RegistryObject<Codec<AddTableLootModifier>> LOOT_TABLE_ADDITION = LOOT_MODIFIERS.register("loot_table_addition", AddTableLootModifier.CODEC);

    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
    }

}
