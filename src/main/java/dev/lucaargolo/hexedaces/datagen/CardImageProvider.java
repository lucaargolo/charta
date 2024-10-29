package dev.lucaargolo.hexedaces.datagen;

import dev.lucaargolo.hexedaces.HexedAces;
import dev.lucaargolo.hexedaces.utils.CardImageUtils;
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

public class CardImageProvider implements DataProvider {

    private final PackOutput output;

    public CardImageProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return CompletableFuture.runAsync(() -> {
            Path outputPath = this.output.getOutputFolder();
            String cardsOutputPath = outputPath + File.separator + "data" + File.separator + HexedAces.MOD_ID + File.separator + "cards";
            try {
                URL resource = HexedAces.class.getClassLoader().getResource("cards");
                URI uri = Objects.requireNonNull(resource).toURI();

                try (Stream<Path> paths = Files.walk(Paths.get(uri), 1)) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String cardName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                        File cardOutputFolder = new File(cardsOutputPath + File.separator + cardName);
                        cardOutputFolder.mkdirs();
                        File cardOutputFile = new File(cardOutputFolder + File.separator + cardName);
                        try (InputStream stream = Files.newInputStream(path)) {
                            CardImageUtils.saveCards(ImageIO.read(stream), cardOutputFile, cachedOutput);
                        }catch (Exception e) {
                            HexedAces.LOGGER.error("Error loading image: {}", path, e);
                        }
                    });
                }
            } catch (URISyntaxException | IOException e) {
                HexedAces.LOGGER.error("Error loading cards", e);
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "Cards";
    }

}
