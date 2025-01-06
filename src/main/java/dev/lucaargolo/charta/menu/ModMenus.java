package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsMenu;
import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModMenus {

    private static final Map<ResourceLocation, MenuType<?>> CONTAINERS = new HashMap<>();

    private static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> RAW_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf in) {
            return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(in.readByteArray()), in.registryAccess());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf out, RegistryFriendlyByteBuf in) {
            out.writeByteArray(in.array());
        }
    };

    public static final MenuType<CrazyEightsMenu> CRAZY_EIGHTS = register("crazy_eights", () -> new ExtendedScreenHandlerType<>(CrazyEightsMenu::new, RAW_CODEC));
    public static final MenuType<FunMenu> FUN = register("fun", () -> new ExtendedScreenHandlerType<>(FunMenu::new, RAW_CODEC));
    public static final MenuType<SolitaireMenu> SOLITAIRE = register("solitaire", () -> new ExtendedScreenHandlerType<>(SolitaireMenu::new, RAW_CODEC));

    private static <M extends AbstractContainerMenu, T extends MenuType<M>> T register(String id, Supplier<T> container) {
        T obj = container.get();
        CONTAINERS.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        CONTAINERS.forEach((id, container) -> Registry.register(BuiltInRegistries.MENU, id, container));
    }

}
