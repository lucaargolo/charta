package dev.lucaargolo.charta.resources;


import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.SuitImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class CardSuitResource implements ResourceManagerReloadListener {

    private HashMap<ResourceLocation, SuitImage> images = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        images.clear();
        manager.listResources("images/suit", id -> id.getPath().endsWith(".mcsuit")).forEach((id, resource) -> {
            try(InputStream stream = resource.open()) {
                ResourceLocation location = id.withPath(s -> s.replace("images/suit/", "").replace(".mcsuit", ""));
                images.put(location, SuitImage.decompress(stream.readAllBytes()));
            }catch (IOException e) {
                Charta.LOGGER.error("Error while reading card suit {} :", id, e);
            }
        });
        Charta.LOGGER.info("Loaded {} card suits", images.size());
    }

    public HashMap<ResourceLocation, SuitImage> getImages() {
        return images;
    }

    public void setImages(HashMap<ResourceLocation, SuitImage> images) {
        this.images = images;
    }
}
