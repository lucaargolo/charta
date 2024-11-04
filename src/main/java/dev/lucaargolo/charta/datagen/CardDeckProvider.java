package dev.lucaargolo.charta.datagen;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CardDeckProvider implements DataProvider {

    private static final List<CardDeck> DECKS = List.of(
        CardDeck.simple(Charta.id("standard"), Charta.id("black")),
        CardDeck.simple(Charta.id("standard"), Charta.id("blue")),
        CardDeck.simple(Charta.id("standard"), Charta.id("green")),
        CardDeck.simple(Charta.id("standard"), Charta.id("red")),
        CardDeck.simple(Charta.id("standard"), Charta.id("yellow")),
        CardDeck.simple(Charta.id("inverted"), Charta.id("inverted")),
        CardDeck.fun(Charta.id("fun"), Charta.id("fun"))
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
                String key = deck.getTranslatableKey();
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