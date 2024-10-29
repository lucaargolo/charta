package dev.lucaargolo.charta.utils;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.blaze3d.platform.NativeImage;
import dev.lucaargolo.charta.Charta;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.data.CachedOutput;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CardImageUtils {

    public static CardImage EMPTY = new CardImage();
    public static StreamCodec<ByteBuf, CardImage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, CardImage::compress, CardImage::decompress);

    static {
        for(int x = 0; x < CardImage.WIDTH; x++) {
            for(int y = 0; y < CardImage.HEIGHT; y++) {
                EMPTY.setPixel(x, y, x > CardImage.WIDTH/2 ? y > CardImage.HEIGHT/2 ? 0 : 58 : y > CardImage.HEIGHT/2 ? 58 : 0, 3);
            }
        }
    }

    public static DynamicTexture convertCardImage(CardImage cardImage) {
        NativeImage nativeImage = new NativeImage(CardImage.WIDTH, CardImage.HEIGHT, true);
        for(int x = 0; x < CardImage.WIDTH; x++) {
            for(int y = 0; y < CardImage.HEIGHT; y++) {
                int argbColor = cardImage.getARGBPixel(x, y);
                int alpha = (argbColor >> 24) & 0xFF;
                int red = (argbColor >> 16) & 0xFF;
                int green = (argbColor >> 8) & 0xFF;
                int blue = argbColor & 0xFF;
                int abgrColor = (alpha << 24) | (blue << 16) | (green << 8) | red;
                nativeImage.setPixelRGBA(x, y, abgrColor);
            }
        }
        return new DynamicTexture(nativeImage);
    }

    public static void saveCards(BufferedImage image, File outputFile, CachedOutput cachedOutput) {
        CardImage[] cards = CardImage.generateCards(image);
        int rows = image.getWidth() / CardImage.WIDTH;
        int cols = image.getHeight() / CardImage.HEIGHT;
        if(rows == cols && cols == 1) {
            CardImage cardImage = cards[0];
            File fileToSave = new File(outputFile.getAbsolutePath() + ".mccard");
            saveCard(cardImage, fileToSave, cachedOutput);
        }else{
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    CardImage cardImage = cards[col * rows + row];
                    File fileToSave = new File(outputFile.getAbsolutePath() + File.separator + (col + 1) + "_" + (row + 1) + ".mccard");
                    saveCard(cardImage, fileToSave, cachedOutput);
                }
            }
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private static void saveCard(CardImage cardImage, File fileToSave, CachedOutput cachedOutput) {
        try {
            Charta.LOGGER.info("Saving file: {}", fileToSave.getAbsoluteFile());
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            HashingOutputStream hashedOutStream = new HashingOutputStream(Hashing.sha1(), outStream);
            cardImage.saveToStream(hashedOutStream);
            cachedOutput.writeIfNeeded(fileToSave.toPath(), outStream.toByteArray(), hashedOutStream.hash());
        } catch (IOException e) {
            Charta.LOGGER.error("Error saving file: {}", fileToSave.getAbsoluteFile(), e);
        }
    }

}
