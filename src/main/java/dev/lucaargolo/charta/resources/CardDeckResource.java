package dev.lucaargolo.charta.resources;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CardDeckResource implements ResourceManagerReloadListener {

    private HashMap<ResourceLocation, CardDeck> decks = new HashMap<>();

    private final String path;

    public CardDeckResource(String path) {
        this.path = path;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        decks.clear();
        manager.listResources(path, id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            try(InputStream stream = resource.open()) {
                ResourceLocation location = id.withPath(s -> s.replace(path + "/", "").replace(".json", ""));
                try(InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    DataResult<CardDeck> cardDeck = CardDeck.CODEC.parse(JsonOps.INSTANCE, json);
                    decks.put(location, cardDeck.getOrThrow());
                }
            }catch (IOException e) {
                Charta.LOGGER.error("Error while reading {} deck {} :", path, id, e);
            }
        });
        Charta.LOGGER.info("Loaded {} {} decks", decks.size(), path);
    }

    public HashMap<ResourceLocation, CardDeck> getDecks() {
        return decks;
    }

    public void setDecks(HashMap<ResourceLocation, CardDeck> decks) {
        this.decks = decks;
    }
}
