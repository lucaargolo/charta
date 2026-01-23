package dev.lucaargolo.charta.common.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SuitImage extends CardImage{

    public static final int WIDTH = 13;
    public static final int HEIGHT = 13;

    public SuitImage() {
        this(new byte[WIDTH * HEIGHT], WIDTH, HEIGHT);
    }

    private SuitImage(byte[] pixels) {
        this(pixels, WIDTH, HEIGHT);
    }

    protected SuitImage(byte[] pixels, int width, int height) {
        super(pixels, width, height);
    }

    public static SuitImage loadFromFile(File file) throws IOException {
        return loadFromFile(file, SuitImage.WIDTH, SuitImage.HEIGHT, SuitImage::new);
    }

    public static SuitImage decompress(byte[] data) {
        return decompress(data, SuitImage.WIDTH, SuitImage.HEIGHT, SuitImage::new);
    }

    public static void saveSuits(BufferedImage image, File outputFile, BiConsumer<File, CardImage> saveConsumer, Function<Integer, String> columnNaming, Function<Integer, String> rowNaming) {
        saveImages(image, SuitImage.WIDTH, SuitImage.HEIGHT, outputFile, "mcsuit", saveConsumer, columnNaming, rowNaming);
    }

}
