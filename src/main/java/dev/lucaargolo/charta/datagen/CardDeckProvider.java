package dev.lucaargolo.charta.datagen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.Charta;
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

public class CardDeckProvider implements DataProvider {

    private final PackOutput output;
    private final Gson gson;

    public static final Map<ResourceLocation, Deck> DECKS = new HashMap<>();
    public static final Map<String, List<ResourceLocation>> GROUPS = new HashMap<>();

    static {
        register(Charta.id("standard/black"), Deck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/black")));
        register(Charta.id("standard/blue"), Deck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/blue")));
        register(Charta.id("standard/green"), Deck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/green")));
        register(Charta.id("standard/red"), Deck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/red")));
        register(Charta.id("standard/yellow"), Deck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/yellow")));

        register(Charta.id("light/aqua"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/aqua")));
        register(Charta.id("light/blue"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/blue")));
        register(Charta.id("light/green"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/green")));
        register(Charta.id("light/orange"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/orange")));
        register(Charta.id("light/pink"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/pink")));
        register(Charta.id("light/red"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/red")));
        register(Charta.id("light/yellow"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/yellow")));

        register(Charta.id("dark/aqua"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/aqua")));
        register(Charta.id("dark/blue"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/blue")));
        register(Charta.id("dark/green"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/green")));
        register(Charta.id("dark/orange"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/orange")));
        register(Charta.id("dark/pink"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/pink")));
        register(Charta.id("dark/red"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/red")));
        register(Charta.id("dark/yellow"), Deck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/yellow")));

        register(Charta.id("inverted"), Deck.simple(Rarity.RARE, true, Charta.id("inverted"), Charta.id("inverted")));

        register(Charta.id("fun"), Deck.fun(Rarity.RARE, true, Charta.id("fun"), Charta.id("fun")));
        register(Charta.id("fun_minimal"), Deck.fun(Rarity.RARE, true, Charta.id("fun"), Charta.id("fun_minimal"), Charta.id("fun_minimal")));
        register(Charta.id("fun_light"), Deck.fun(Rarity.RARE, true, Charta.id("fun_light"), Charta.id("fun_light")));

        register(Charta.id("flags/argentina"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/argentina")));
        register(Charta.id("flags/australia"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/australia")));
        register(Charta.id("flags/belgium"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/belgium")));
        register(Charta.id("flags/brazil"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/brazil")));
        register(Charta.id("flags/canada"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/canada")));
        register(Charta.id("flags/china"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/china")));
        register(Charta.id("flags/france"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/france")));
        register(Charta.id("flags/germany"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/germany")));
        register(Charta.id("flags/india"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/india")));
        register(Charta.id("flags/indonesia"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/indonesia")));
        register(Charta.id("flags/ireland"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/ireland")));
        register(Charta.id("flags/italy"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/italy")));
        register(Charta.id("flags/japan"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/japan")));
        register(Charta.id("flags/mexico"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/mexico")));
        register(Charta.id("flags/netherlands"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/netherlands")));
        register(Charta.id("flags/philippines"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/philippines")));
        register(Charta.id("flags/poland"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/poland")));
        register(Charta.id("flags/portugal"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/portugal")));
        register(Charta.id("flags/rainbow"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/rainbow")));
        register(Charta.id("flags/russia"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/russia")));
        register(Charta.id("flags/spain"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/spain")));
        register(Charta.id("flags/thailand"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/thailand")));
        register(Charta.id("flags/ukraine"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/ukraine")));
        register(Charta.id("flags/united_kingdom"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/united_kingdom")));
        register(Charta.id("flags/usa"), Deck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/usa")));

        register(Charta.id("neon/aqua"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/aqua")));
        register(Charta.id("neon/blue"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/blue")));
        register(Charta.id("neon/green"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/green")));
        register(Charta.id("neon/orange"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/orange")));
        register(Charta.id("neon/pink"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/pink")));
        register(Charta.id("neon/red"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/red")));
        register(Charta.id("neon/yellow"), Deck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/yellow")));

        register(Charta.id("fun_inverted"), Deck.fun(Rarity.UNCOMMON, false, Charta.id("fun_inverted"), Charta.id("fun_inverted")));
        register(Charta.id("fun_classic"), Deck.fun(Rarity.UNCOMMON, false, Charta.id("fun_classic"), Charta.id("fun_classic")));

        register(Charta.id("fun_neon"), Deck.fun(Rarity.RARE, false, Charta.id("fun_neon"), Charta.id("fun_neon")));
        register(Charta.id("fun_minimal_neon"), Deck.fun(Rarity.RARE, false, Charta.id("fun_neon"), Charta.id("fun_minimal_neon"), Charta.id("fun_minimal_neon")));

        register(Charta.id("metals/copper"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/copper"), Charta.id("metals/copper")));
        register(Charta.id("metals/iron"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/iron"), Charta.id("metals/iron")));
        register(Charta.id("metals/gold"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/gold"), Charta.id("metals/gold")));

        register(Charta.id("gems/diamond"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/diamond"), Charta.id("gems/diamond")));
        register(Charta.id("gems/emerald"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/emerald"), Charta.id("gems/emerald")));
        register(Charta.id("gems/ruby"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/ruby"), Charta.id("gems/ruby")));
        register(Charta.id("gems/sapphire"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/sapphire"), Charta.id("gems/sapphire")));
        register(Charta.id("gems/amethyst"), Deck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/amethyst"), Charta.id("gems/amethyst")));
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
    
    public CardDeckProvider(PackOutput output) {
        this.output = output;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            Path outputPath = this.output.getOutputFolder();
            String decksOutputPath = outputPath + File.separator + "data" + File.separator + Charta.MOD_ID + File.separator + "decks";
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
                    Charta.LOGGER.error("Error writing stream for file: {}", fullPath, e);
                }
                try {
                    cachedOutput.writeIfNeeded(fullPath, outStream.toByteArray(), hashedOutStream.hash());
                } catch (IOException e) {
                    Charta.LOGGER.error("Error saving file: {}", fullPath, e);
                }
            });
        });
    }

    @Override
    public @NotNull String getName() {
        return "CardDecks";
    }

}
