package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
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
            String cardsOutputPath = outputPath + File.separator + "data" + File.separator + Charta.MOD_ID + File.separator + "images" + File.separator + "card";
            try {
                URL resource = Charta.class.getClassLoader().getResource("cards");
                URI uri = Objects.requireNonNull(resource).toURI();

                try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                    paths.filter(Files::isRegularFile).forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String cardName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                        String subFolder = path.getParent().toString().replace("cards", "");
                        File cardOutputFolder = new File(cardsOutputPath + File.separator + subFolder + File.separator + cardName);
                        cardOutputFolder.mkdirs();
                        try (InputStream stream = Files.newInputStream(path)) {
                            CardImage.saveCards(ImageIO.read(stream), cardOutputFolder, (fileToSave, cardImage) -> {
                                CardImageUtils.saveImage(cardImage, fileToSave, cachedOutput);
                            }, col -> {
                                return switch (col) {
                                    case 1 -> fileName.contains("fun") ? "red" : "spades";
                                    case 2 -> fileName.contains("fun") ? "yellow" : "hearts";
                                    case 3 -> fileName.contains("fun") ? "green" : "clubs";
                                    case 4 -> fileName.contains("fun") ? "blue" : "diamonds";
                                    default -> "";
                                };
                            }, col -> {
                                return switch (col) {
                                    case 0 -> fileName.contains("fun") ? "wild" : "blank";
                                    case 1 -> fileName.contains("fun") ? "one" : "ace";
                                    case 2 -> "two";
                                    case 3 -> "three";
                                    case 4 -> "four";
                                    case 5 -> "five";
                                    case 6 -> "six";
                                    case 7 -> "seven";
                                    case 8 -> "eight";
                                    case 9 -> "nine";
                                    case 10 -> fileName.contains("fun") ? "zero" : "ten";
                                    case 11 -> fileName.contains("fun") ? "block" : "jack";
                                    case 12 -> fileName.contains("fun") ? "reverse" : "queen";
                                    case 13 -> fileName.contains("fun") ? "plustwo" : "king";
                                    case 14 -> fileName.contains("fun") ? "wildplusfour" : "joker";
                                    default -> "";
                                };
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
        return "Cards";
    }

}
