package dev.lucaargolo.hexedaces.utils;

import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class CardImage {

    public static final int WIDTH = 25;
    public static final int HEIGHT = 35;
    private static final int TOTAL_PIXELS = WIDTH * HEIGHT;

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

    public CardImage() {
        this.pixels = new byte[TOTAL_PIXELS];
    }

    public CardImage copy() {
        CardImage copy = new CardImage();
        System.arraycopy(this.pixels, 0, copy.pixels, 0, TOTAL_PIXELS);
        return copy;
    }

    public void setARGBPixel(int x, int y, int argb) {
        int rgb = argb & 0x00FFFFFF;
        int alpha = (argb >> 24) & 0xFF;

        this.setPixel(x, y, findClosestColorIndex(rgb), findClosestAlphaIndex(alpha));
    }

    public void setPixel(int x, int y, int colorIndex, int alphaIndex) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            throw new IllegalArgumentException("Invalid pixel coordinates.");
        if (colorIndex < 0 || colorIndex >= 64)
            throw new IllegalArgumentException("Color index out of range.");
        if (alphaIndex < 0 || alphaIndex >= 4)
            throw new IllegalArgumentException("Alpha index out of range.");

        byte pixelByte = (byte)((alphaIndex << 6) | (colorIndex & 0x3F));
        pixels[y * WIDTH + x] = pixelByte;
    }

    public int getARGBPixel(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            throw new IllegalArgumentException("Invalid pixel coordinates.");

        byte pixelByte = pixels[y * WIDTH + x];
        int alphaIndex = (pixelByte >> 6) & 0x03;
        int colorIndex = pixelByte & 0x3F;

        int rgb = COLOR_PALETTE[colorIndex];
        int alpha = ALPHA_PALETTE[alphaIndex];

        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public byte getPixel(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            throw new IllegalArgumentException("Invalid pixel coordinates.");

        return pixels[y * WIDTH + x];
    }

    public NativeImage toNativeImage() {
        NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
        for(int x = 0; x < WIDTH; x++ ) {
            for(int y = 0; y < HEIGHT; y++ ) {
                image.setPixelRGBA(x, y, this.getARGBPixel(x, y));
            }
        }

        return image;
    }

    private int findClosestColorIndex(int rgb) {
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

    private int findClosestAlphaIndex(int alpha) {
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

    public void saveToFile(String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pixels);
        }
    }

    public static CardImage loadFromFile(File file) throws IOException {
        CardImage image = new CardImage();
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.read(image.pixels) != TOTAL_PIXELS) {
                throw new IOException("Invalid file size, expected "+TOTAL_PIXELS+" bytes.");
            }
        }
        return image;
    }

    public static void generateAtlas(File inputFile, File outputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        int width = image.getWidth();
        int height = image.getHeight();

        int rows = width / CardImage.WIDTH;
        int cols = height / CardImage.HEIGHT;


        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                CardImage cardImage = new CardImage();

                for (int x = 0; x < CardImage.WIDTH; x++) {
                    for (int y = 0; y < CardImage.HEIGHT; y++) {
                        int argb;
                        try {
                            argb = image.getRGB(x + (row*CardImage.WIDTH), y + (col*CardImage.HEIGHT));
                        }catch (ArrayIndexOutOfBoundsException e) {
                            argb = 0xFFFFFFFF;
                        }
                        cardImage.setARGBPixel(x, y, argb);
                    }
                }

                File fileToSave = new File(outputFile.getAbsolutePath() + "_" + (col+1) + "_" + (row+1) + ".mccard");
                try {
                    cardImage.saveToFile(fileToSave.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
