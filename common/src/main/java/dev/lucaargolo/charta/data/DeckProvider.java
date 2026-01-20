package dev.lucaargolo.charta.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeckProvider implements DataProvider {

    private final PackOutput output;
    private final Gson gson;

    public static final Map<ResourceLocation, Deck> DECKS = new HashMap<>();
    public static final Map<String, List<ResourceLocation>> GROUPS = new HashMap<>();

    public static final Deck FUN_INVERTED = register(ChartaMod.id("fun_inverted"), Deck.fun(Rarity.UNCOMMON, false, ChartaMod.id("fun_inverted"), ChartaMod.id("fun_inverted")));
    public static final Deck FUN_CLASSIC = register(ChartaMod.id("fun_classic"), Deck.fun(Rarity.UNCOMMON, false, ChartaMod.id("fun_classic"), ChartaMod.id("fun_classic")));

    public static final Deck FUN_NEON = register(ChartaMod.id("fun_neon"), Deck.fun(Rarity.RARE, false, ChartaMod.id("fun_neon"), ChartaMod.id("fun_neon")));
    public static final Deck FUN_MINIMAL_NEON = register(ChartaMod.id("fun_minimal_neon"), Deck.fun(Rarity.RARE, false, ChartaMod.id("fun_neon"), ChartaMod.id("fun_minimal_neon"), ChartaMod.id("fun_minimal_neon")));

    static {
        register(ChartaMod.id("standard/black"), Deck.simple(Rarity.COMMON, true, ChartaMod.id("standard"), ChartaMod.id("standard/black")));
        register(ChartaMod.id("standard/blue"), Deck.simple(Rarity.COMMON, true, ChartaMod.id("standard"), ChartaMod.id("standard/blue")));
        register(ChartaMod.id("standard/green"), Deck.simple(Rarity.COMMON, true, ChartaMod.id("standard"), ChartaMod.id("standard/green")));
        register(ChartaMod.id("standard/red"), Deck.simple(Rarity.COMMON, true, ChartaMod.id("standard"), ChartaMod.id("standard/red")));
        register(ChartaMod.id("standard/yellow"), Deck.simple(Rarity.COMMON, true, ChartaMod.id("standard"), ChartaMod.id("standard/yellow")));

        register(ChartaMod.id("light/aqua"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/aqua")));
        register(ChartaMod.id("light/blue"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/blue")));
        register(ChartaMod.id("light/green"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/green")));
        register(ChartaMod.id("light/orange"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/orange")));
        register(ChartaMod.id("light/pink"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/pink")));
        register(ChartaMod.id("light/red"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/red")));
        register(ChartaMod.id("light/yellow"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("light/yellow")));

        register(ChartaMod.id("dark/aqua"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/aqua")));
        register(ChartaMod.id("dark/blue"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/blue")));
        register(ChartaMod.id("dark/green"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/green")));
        register(ChartaMod.id("dark/orange"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/orange")));
        register(ChartaMod.id("dark/pink"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/pink")));
        register(ChartaMod.id("dark/red"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/red")));
        register(ChartaMod.id("dark/yellow"), Deck.simple(Rarity.UNCOMMON, true, ChartaMod.id("light"), ChartaMod.id("dark"), ChartaMod.id("dark/yellow")));

        register(ChartaMod.id("inverted"), Deck.simple(Rarity.RARE, true, ChartaMod.id("inverted"), ChartaMod.id("inverted")));

        register(ChartaMod.id("fun"), Deck.fun(Rarity.RARE, true, ChartaMod.id("fun"), ChartaMod.id("fun")));
        register(ChartaMod.id("fun_minimal"), Deck.fun(Rarity.RARE, true, ChartaMod.id("fun"), ChartaMod.id("fun_minimal"), ChartaMod.id("fun_minimal")));
        register(ChartaMod.id("fun_light"), Deck.fun(Rarity.RARE, true, ChartaMod.id("fun_light"), ChartaMod.id("fun_light")));

        register(ChartaMod.id("flags/argentina"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/argentina")));
        register(ChartaMod.id("flags/australia"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/australia")));
        register(ChartaMod.id("flags/belgium"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/belgium")));
        register(ChartaMod.id("flags/brazil"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/brazil")));
        register(ChartaMod.id("flags/canada"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/canada")));
        register(ChartaMod.id("flags/china"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/china")));
        register(ChartaMod.id("flags/france"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/france")));
        register(ChartaMod.id("flags/germany"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/germany")));
        register(ChartaMod.id("flags/india"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/india")));
        register(ChartaMod.id("flags/indonesia"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/indonesia")));
        register(ChartaMod.id("flags/ireland"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/ireland")));
        register(ChartaMod.id("flags/italy"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/italy")));
        register(ChartaMod.id("flags/japan"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/japan")));
        register(ChartaMod.id("flags/mexico"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/mexico")));
        register(ChartaMod.id("flags/netherlands"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/netherlands")));
        register(ChartaMod.id("flags/philippines"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/philippines")));
        register(ChartaMod.id("flags/poland"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/poland")));
        register(ChartaMod.id("flags/portugal"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/portugal")));
        register(ChartaMod.id("flags/rainbow"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/rainbow")));
        register(ChartaMod.id("flags/russia"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/russia")));
        register(ChartaMod.id("flags/spain"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/spain")));
        register(ChartaMod.id("flags/thailand"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/thailand")));
        register(ChartaMod.id("flags/ukraine"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/ukraine")));
        register(ChartaMod.id("flags/united_kingdom"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/united_kingdom")));
        register(ChartaMod.id("flags/usa"), Deck.simple(Rarity.COMMON, false, ChartaMod.id("standard"), ChartaMod.id("flags/usa")));

        register(ChartaMod.id("neon/aqua"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/aqua")));
        register(ChartaMod.id("neon/blue"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/blue")));
        register(ChartaMod.id("neon/green"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/green")));
        register(ChartaMod.id("neon/orange"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/orange")));
        register(ChartaMod.id("neon/pink"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/pink")));
        register(ChartaMod.id("neon/red"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/red")));
        register(ChartaMod.id("neon/yellow"), Deck.simple(Rarity.UNCOMMON, false, ChartaMod.id("neon"), ChartaMod.id("neon/yellow")));

        register(ChartaMod.id("metals/copper"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("metals/copper"), ChartaMod.id("metals/copper")));
        register(ChartaMod.id("metals/iron"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("metals/iron"), ChartaMod.id("metals/iron")));
        register(ChartaMod.id("metals/gold"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("metals/gold"), ChartaMod.id("metals/gold")));

        register(ChartaMod.id("gems/diamond"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("gems/diamond"), ChartaMod.id("gems/diamond")));
        register(ChartaMod.id("gems/emerald"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("gems/emerald"), ChartaMod.id("gems/emerald")));
        register(ChartaMod.id("gems/ruby"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("gems/ruby"), ChartaMod.id("gems/ruby")));
        register(ChartaMod.id("gems/sapphire"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("gems/sapphire"), ChartaMod.id("gems/sapphire")));
        register(ChartaMod.id("gems/amethyst"), Deck.simple(Rarity.RARE, false, ChartaMod.id("metallic"), ChartaMod.id("gems/amethyst"), ChartaMod.id("gems/amethyst")));
    }

    public static Deck register(ResourceLocation resourceLocation, Deck deck) {
        if(DECKS.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Duplicate resource location: " + resourceLocation);
        }
        if(resourceLocation.getPath().split("/").length == 2) {
            String group = resourceLocation.getPath().split("/")[0];
            GROUPS.computeIfAbsent(group, k -> new ArrayList<>()).add(resourceLocation);
        }
        DECKS.put(resourceLocation, deck);
        return deck;
    }
    
    public DeckProvider(PackOutput output) {
        this.output = output;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            Path outputPath = this.output.getOutputFolder();
            String decksOutputPath = outputPath + File.separator + "data" + File.separator + ChartaMod.MOD_ID + File.separator + "decks";
            DECKS.forEach((key, deck) -> {
                String path = key.getPath() + ".json";
                Path fullPath = Path.of(decksOutputPath, path);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                HashingOutputStream hashedOutStream = new HashingOutputStream(Hashing.sha1(), outStream);
                DataResult<JsonElement> json = Deck.CODEC.encode(deck, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
                try (OutputStreamWriter writer = new OutputStreamWriter(hashedOutStream, StandardCharsets.UTF_8)) {
                    writer.write(gson.toJson(json.getOrThrow()));
                    writer.flush();
                } catch (IOException e) {
                    ChartaMod.LOGGER.error("Error writing stream for file: {}", fullPath, e);
                }
                try {
                    cachedOutput.writeIfNeeded(fullPath, outStream.toByteArray(), hashedOutStream.hash());
                } catch (IOException e) {
                    ChartaMod.LOGGER.error("Error saving file: {}", fullPath, e);
                }
            });
        });
    }

    @Override
    public @NotNull String getName() {
        return "CardDecks";
    }

}
