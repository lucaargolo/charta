package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DeckImageProvider implements DataProvider {

    private final FabricDataOutput output;

    public DeckImageProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            Path outputPath = this.output.getOutputFolder();
            String decksOutputPath = outputPath + File.separator + "data" + File.separator + Charta.MOD_ID + File.separator + "images" + File.separator + "deck";
            try {
                URL resource = Charta.class.getClassLoader().getResource("decks");
                URI uri = Objects.requireNonNull(resource).toURI();

                try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String cardName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                        String parentFolder = path.getParent().toString();
                        String subFolder = parentFolder.substring(parentFolder.indexOf("decks") + "decks".length());
                        File cardOutputFolder = new File(decksOutputPath);
                        cardOutputFolder.mkdirs();
                        File cardOutputFile = new File(cardOutputFolder + File.separator + subFolder + File.separator + cardName);
                        try (InputStream stream = Files.newInputStream(path)) {
                            CardImage.saveCards(ImageIO.read(stream), cardOutputFile, (fileToSave, cardImage) -> {
                                CardImageUtils.saveImage(cardImage, fileToSave, cachedOutput);
                            });
                        }catch (Exception e) {
                            Charta.LOGGER.error("Error loading image: {}", path, e);
                        }
                    });
                }
            } catch (URISyntaxException | IOException e) {
                Charta.LOGGER.error("Error loading cards", e);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "Decks";
    }

}
