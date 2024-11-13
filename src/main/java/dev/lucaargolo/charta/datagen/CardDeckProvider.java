package dev.lucaargolo.charta.datagen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CardDeckProvider implements DataProvider {

    private static final List<CardDeck> DECKS = List.of(
        CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/black")),
        CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/blue")),
        CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/green")),
        CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/red")),
        CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/yellow")),

        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/aqua")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/blue")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/green")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/orange")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/pink")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/red")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/yellow")),

        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/aqua")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/blue")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/green")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/orange")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/pink")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/red")),
        CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/yellow")),

        CardDeck.simple(Rarity.RARE, true, Charta.id("inverted"), Charta.id("inverted")),
        CardDeck.fun(Rarity.RARE, true, Charta.id("fun"), Charta.id("fun")),

        CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("metallic"), Charta.id("metals/copper"), Charta.id("metals/copper")),
        CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("metallic"), Charta.id("metals/iron"), Charta.id("metals/iron")),
        CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("metallic"), Charta.id("metals/gold"), Charta.id("metals/gold")),

        CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/diamond"), Charta.id("gems/diamond")),
        CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/emerald"), Charta.id("gems/emerald")),
        CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/ruby"), Charta.id("gems/ruby")),
        CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/sapphire"), Charta.id("gems/sapphire")),
        CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/amethyst"), Charta.id("gems/amethyst"))

    );

    private final PackOutput output;
    private final Gson gson;

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
            DECKS.forEach(deck -> {
                String key = deck.getDeckTranslatableKey();
                String path = key.replace("deck." + Charta.MOD_ID + "." , "").replace(".", File.separator) + ".json";
                Path fullPath = Path.of(decksOutputPath, path);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                HashingOutputStream hashedOutStream = new HashingOutputStream(Hashing.sha1(), outStream);
                DataResult<JsonElement> json = CardDeck.CODEC.encode(deck, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
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
