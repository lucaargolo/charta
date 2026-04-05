package dev.lucaargolo.charta.common.addon;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.Games;
import dev.lucaargolo.charta.common.game.api.game.Game;
import dev.lucaargolo.charta.common.game.api.game.GameType;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.menu.ModMenuTypes;
import dev.lucaargolo.charta.common.network.ModPacketManager;
import dev.lucaargolo.charta.common.registry.ModMenuTypeRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Central registration point for Charta addons.
 *
 * All registrations are deferred and applied during ChartaMod.init(),
 * so it is safe to call these methods at any point during mod loading —
 * even before ChartaMod itself has initialized.
 *
 * IMPORTANT: Do NOT pass eagerly-evaluated values that trigger Charta static
 * initializers (e.g. AbstractCardMenu.Definition.STREAM_CODEC) directly.
 * Use the Supplier-based overloads instead.
 */
public final class ChartaAddonRegistry {

    private ChartaAddonRegistry() {}

    private static final List<DeferredGame<?, ?>>  deferredGames   = new ArrayList<>();
    private static final List<DeferredMenu<?, ?>>  deferredMenus   = new ArrayList<>();
    private static final List<DeferredPacket<?>>   deferredPackets = new ArrayList<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /** Register a game type. Safe to call before ChartaMod.init(). */
    public static <G extends Game<G, M>, M extends AbstractCardMenu<G, M>>
    AddonEntry<MinecraftEntry<GameType<G, M>>> registerGame(String name, Supplier<GameType<G, M>> factory) {
        AddonEntry<MinecraftEntry<GameType<G, M>>> holder = new AddonEntry<>();
        deferredGames.add(new DeferredGame<>(name, factory, holder));
        return holder;
    }

    /**
     * Register a menu type. Safe to call before ChartaMod.init().
     * Pass the streamCodec as a Supplier to avoid triggering static initializers early.
     * Example: {@code () -> AbstractCardMenu.Definition.STREAM_CODEC}
     */
    public static <M extends AbstractCardMenu<?, ?>, D>
    AddonEntry<ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D>> registerMenu(
            String name,
            TriFunction<Integer, Inventory, D, M> factory,
            Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodecSupplier) {
        AddonEntry<ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D>> holder = new AddonEntry<>();
        deferredMenus.add(new DeferredMenu<>(name, factory, streamCodecSupplier, holder));
        return holder;
    }

    /** Register a network packet. Safe to call before ChartaMod.init(). */
    public static <T extends CustomPacketPayload> void registerPacket(
            ModPacketManager.PacketDirection direction,
            Class<T> klass) {
        deferredPackets.add(new DeferredPacket<>(direction, klass));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void applyDeferredRegistrations() {
        ChartaMod.LOGGER.info("[ChartaAddon] Applying {} deferred game(s) and {} deferred menu(s)",
                deferredGames.size(), deferredMenus.size());
        for (DeferredGame<?, ?> dg : deferredGames) ((DeferredGame) dg).apply();
        deferredGames.clear();
        for (DeferredMenu<?, ?> dm : deferredMenus) ((DeferredMenu) dm).apply();
        deferredMenus.clear();
    }

    public static void applyDeferredPackets(ModPacketManager manager) {
        for (DeferredPacket<?> dp : deferredPackets) dp.apply(manager);
        deferredPackets.clear();
    }

    // ── Holder ────────────────────────────────────────────────────────────────

    public static final class AddonEntry<T> {
        private final AtomicReference<T> ref = new AtomicReference<>();
        void set(T value) { ref.set(value); }
        public T get() {
            T value = ref.get();
            if (value == null) throw new IllegalStateException("AddonEntry not yet initialized — ChartaMod.init() has not run");
            return value;
        }
    }

    // ── Deferred records ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static class DeferredGame<G extends Game<G, M>, M extends AbstractCardMenu<G, M>> {
        private final String name;
        private final Supplier<GameType<G, M>> factory;
        private final AddonEntry<MinecraftEntry<GameType<G, M>>> holder;

        DeferredGame(String name, Supplier<GameType<G, M>> factory, AddonEntry<MinecraftEntry<GameType<G, M>>> holder) {
            this.name = name; this.factory = factory; this.holder = holder;
        }

        void apply() {
            holder.set((MinecraftEntry<GameType<G, M>>) (Object) Games.MOD_REGISTRY.register(name, factory));
        }
    }

    private static class DeferredMenu<M extends AbstractCardMenu<?, ?>, D> {
        private final String name;
        private final TriFunction<Integer, Inventory, D, M> factory;
        private final Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodecSupplier;
        private final AddonEntry<ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D>> holder;

        DeferredMenu(String name, TriFunction<Integer, Inventory, D, M> factory,
                     Supplier<StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodecSupplier,
                     AddonEntry<ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D>> holder) {
            this.name = name; this.factory = factory;
            this.streamCodecSupplier = streamCodecSupplier; this.holder = holder;
        }

        void apply() {
            holder.set(ModMenuTypes.REGISTRY.register(name, factory, streamCodecSupplier.get()));
        }
    }

    private record DeferredPacket<T extends CustomPacketPayload>(
            ModPacketManager.PacketDirection direction, Class<T> klass) {
        void apply(ModPacketManager manager) { manager.registerPacket(direction, klass); }
    }
}
