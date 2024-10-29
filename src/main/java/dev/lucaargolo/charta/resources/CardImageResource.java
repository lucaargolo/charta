package dev.lucaargolo.charta.resources;


import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CardImageResource implements ResourceManagerReloadListener {

    private final HashMap<ResourceLocation, CardImage> images = new HashMap<>();

    private final String path;

    public CardImageResource(String path) {
        this.path = path;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        images.clear();
        manager.listResources(path, id -> id.getPath().endsWith(".mccard")).forEach((id, resource) -> {
            try(InputStream stream = resource.open()) {
                ResourceLocation location = id.withPath(s -> s.replace(path + "/", "").replace(".mccard", ""));
                images.put(location, CardImage.decompress(stream.readAllBytes()));
            }catch (IOException e) {
                Charta.LOGGER.error("Error while reading {} image {} :", path, id, e);
            }
        });
        Charta.LOGGER.info("Loaded {} {} images", images.size(), path);
    }

    public HashMap<ResourceLocation, CardImage> getImages() {
        return images;
    }

}
