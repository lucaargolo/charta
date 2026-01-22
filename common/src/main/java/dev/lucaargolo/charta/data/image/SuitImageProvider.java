package dev.lucaargolo.charta.data.image;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.utils.CardImageUtils;
import dev.lucaargolo.charta.utils.SuitImage;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
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

public class SuitImageProvider implements DataProvider {

    private final PackOutput output;

    public SuitImageProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            Path outputPath = this.output.getOutputFolder();
            String cardsOutputPath = outputPath + File.separator + "data" + File.separator + ChartaMod.MOD_ID + File.separator + "images" + File.separator + "suit";
            try {
                URL resource = ChartaMod.class.getClassLoader().getResource("suits");
                URI uri = Objects.requireNonNull(resource).toURI();

                try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String suitName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                        String parentFolder = path.getParent().toString();
                        String subFolder = parentFolder.substring(parentFolder.indexOf("suits") + "suits".length());
                        File cardOutputFolder = new File(cardsOutputPath + File.separator + subFolder + File.separator + suitName);
                        cardOutputFolder.mkdirs();
                        try (InputStream stream = Files.newInputStream(path)) {
                            SuitImage.saveSuits(ImageIO.read(stream), cardOutputFolder, (fileToSave, suitImage) -> {
                                CardImageUtils.saveImage(suitImage, fileToSave, cachedOutput);
                            }, String::valueOf, row -> {
                                return switch (row) {
                                    case 1 -> fileName.contains("fun") ? "red" : "spades";
                                    case 2 -> fileName.contains("fun") ? "yellow" : "hearts";
                                    case 3 -> fileName.contains("fun") ? "green" : "clubs";
                                    case 4 -> fileName.contains("fun") ? "blue" : "diamonds";
                                    default -> "";
                                };
                            });
                        }catch (Exception e) {
                            ChartaMod.LOGGER.error("Error loading image: {}", path, e);
                        }
                    });
                }
            } catch (URISyntaxException | IOException e) {
                ChartaMod.LOGGER.error("Error loading suits", e);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "Suits";
    }

}
