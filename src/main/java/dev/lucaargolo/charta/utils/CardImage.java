package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.Charta;
import org.apache.commons.lang3.function.TriFunction;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CardImage {

    public static final int WIDTH = 25;
    public static final int HEIGHT = 35;

    public static final int[] COLOR_PALETTE = {
        0x000000, 0x252525, 0x494949, 0x6e6e6e, 0x929292, 0xb7b7b7, 0xdcdcdc, 0xffffff,
        0x7f0000, 0xb20000, 0xe30000, 0xff0000, 0xff5353, 0xff7575, 0xff9898, 0xffbaba,
        0x7f3f00, 0xb25800, 0xe37000, 0xff7f00, 0xffa953, 0xffba75, 0xffcb98, 0xffdcba,
        0x7f7f00, 0xb2b200, 0xe3e300, 0xffff00, 0xffff53, 0xffff75, 0xffff98, 0xffffba,
        0x007f00, 0x00b200, 0x00e300, 0x00ff00, 0x53ff53, 0x75ff75, 0x98ff98, 0xbaffba,
        0x007f7f, 0x00b2b2, 0x00e3e3, 0x00ffff, 0x53ffff, 0x75ffff, 0x98ffff, 0xbaffff,
        0x00007f, 0x0000b2, 0x0000e3, 0x0000ff, 0x5353ff, 0x7575ff, 0x9898ff, 0xbabaff,
        0x7f007f, 0xb200b2, 0xe300e3, 0xff00ff, 0xff53ff, 0xff75ff, 0xff98ff, 0xffbaff,
    };
    public static final int[] ALPHA_PALETTE = {0, 85, 170, 255};

    private final byte[] pixels;
    private final int width;
    private final int height;
    private final int totalPixels;

    public CardImage() {
        this(new byte[WIDTH * HEIGHT], WIDTH, HEIGHT);
    }

    private CardImage(byte[] pixels) {
        this(pixels, WIDTH, HEIGHT);
    }

    protected CardImage(byte[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.totalPixels = width * height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public CardImage copy() {
        CardImage copy = new CardImage();
        System.arraycopy(this.pixels, 0, copy.pixels, 0, totalPixels);
        return copy;
    }

    public boolean isEmpty() {
        for (int i = 0; i < totalPixels; i++) {
            int alphaIndex = (pixels[i] >> 6) & 0x03;
            if(alphaIndex != 0) {
                return false;
            }
        }
        return true;
    }

    public void setARGBPixel(int x, int y, int argb) {
        int rgb = argb & 0x00FFFFFF;
        int alpha = (argb >> 24) & 0xFF;

        this.setPixel(x, y, findClosestColorIndex(rgb), findClosestAlphaIndex(alpha));
    }

    public void setPixel(int x, int y, int colorIndex, int alphaIndex) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException("Invalid pixel coordinates.");
        if (colorIndex < 0 || colorIndex >= 64)
            throw new IllegalArgumentException("Color index out of range.");
        if (alphaIndex < 0 || alphaIndex >= 4)
            throw new IllegalArgumentException("Alpha index out of range.");

        byte pixelByte = (byte)((alphaIndex << 6) | (colorIndex & 0x3F));
        pixels[y * width + x] = pixelByte;
    }

    public int getARGBPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException("Invalid pixel coordinates.");

        byte pixelByte = pixels[y * width + x];
        int alphaIndex = (pixelByte >> 6) & 0x03;
        int colorIndex = pixelByte & 0x3F;

        int rgb = COLOR_PALETTE[colorIndex];
        int alpha = ALPHA_PALETTE[alphaIndex];

        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public byte getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException("Invalid pixel coordinates.");

        return pixels[y * width + x];
    }

    public void saveToFile(String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            saveToStream(fos);
        }
    }

    public void saveToStream(OutputStream stream) throws IOException {
        stream.write(compress());
    }

    public byte[] compress()  {
        try {
            return ZIPCompression.compress(pixels);
        } catch (IOException exception) {
            Charta.LOGGER.error("Error compressing card image: ", exception);
            return new byte[0];
        }
    }

    public static CardImage loadFromFile(File file) throws IOException {
        return loadFromFile(file, CardImage.WIDTH, CardImage.HEIGHT, CardImage::new);
    }

    public static CardImage decompress(byte[] data) {
        return decompress(data, CardImage.WIDTH, CardImage.HEIGHT, CardImage::new);
    }

    public static void saveCards(BufferedImage image, File outputFile, BiConsumer<File, CardImage> saveConsumer) {
        saveImages(image, CardImage.WIDTH, CardImage.HEIGHT, outputFile, "mccard", saveConsumer);
    }

    protected static <I extends CardImage> I loadFromFile(File file, int width, int height, Function<byte[], I> function) throws IOException {
        I image;
        try (FileInputStream fis = new FileInputStream(file)) {
            image = decompress(fis.readAllBytes(), width, height, function);
        }
        return image;
    }

    protected static <I extends CardImage> I decompress(byte[] data, int width, int height, Function<byte[], I> function) {
        try {
            byte[] pixels = ZIPCompression.decompress(data);
            if (pixels.length != width * height) {
                throw new IOException("Invalid file size, expected "+(width*height)+" bytes.");
            }
            return function.apply(pixels);
        } catch (IOException exception) {
            Charta.LOGGER.error("Error decompressing card image: ", exception);
            return function.apply(null);
        }
    }

    protected static void saveImages(BufferedImage image, int width, int height, File outputFile, String extension, BiConsumer<File, CardImage> saveConsumer) {
        CardImage[] cards = generateImages(image, width, height);
        int rows = image.getWidth() / width;
        int cols = image.getHeight() / height;
        if(rows == cols && cols == 1) {
            CardImage cardImage = cards[0];
            File fileToSave = new File(outputFile.getAbsolutePath() + "." + extension);
            saveConsumer.accept(fileToSave, cardImage);
        }else {
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    CardImage cardImage = cards[col * rows + row];
                    if (!cardImage.isEmpty()) {
                        File folderToSave = new File(outputFile.getAbsolutePath());
                        folderToSave.mkdirs();
                        File fileToSave = new File(outputFile.getAbsolutePath() + File.separator + (cols == 1 ? row : col + "_" + row) + "." + extension);
                        saveConsumer.accept(fileToSave, cardImage);
                    }
                }
            }
        }
    }

    protected static CardImage[] generateImages(BufferedImage image, int width, int height) {
        int rows = image.getWidth() / width;
        int cols = image.getHeight() / height;

        CardImage[] cards = new CardImage[rows*cols];
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                CardImage cardImage = new CardImage(new byte[width*height], width, height);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int argb;
                        try {
                            argb = image.getRGB(x + (row*width), y + (col*height));
                        }catch (ArrayIndexOutOfBoundsException e) {
                            argb = 0xFFFFFFFF;
                        }
                        cardImage.setARGBPixel(x, y, argb);
                    }
                }

                cards[col * rows + row] = cardImage;
            }
        }
        return cards;
    }

    public static int findClosestColorIndex(int rgb) {
        int closestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        int r1 = (rgb >> 16) & 0xFF;
        int g1 = (rgb >> 8) & 0xFF;
        int b1 = rgb & 0xFF;

        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            int paletteColor = COLOR_PALETTE[i];
            int r2 = (paletteColor >> 16) & 0xFF;
            int g2 = (paletteColor >> 8) & 0xFF;
            int b2 = paletteColor & 0xFF;

            int distance = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);

            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    public static int findClosestAlphaIndex(int alpha) {
        int closestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < ALPHA_PALETTE.length; i++) {
            int distance = Math.abs(alpha - ALPHA_PALETTE[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CardImage other)) return false;
        return Arrays.equals(this.pixels, other.pixels);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pixels);
    }


}
