package dev.lucaargolo.hexedaces.utils;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import dev.lucaargolo.hexedaces.HexedAces;
import io.netty.buffer.ByteBuf;
import net.minecraft.data.CachedOutput;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CardImageUtils {

    public static StreamCodec<ByteBuf, CardImage> CARD_IMAGE_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, CardImage::getPixels, CardImage::new);

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static void saveCards(BufferedImage image, File outputFile, CachedOutput cachedOutput) {
        CardImage[] cards = CardImage.generateCards(image);
        int rows = image.getWidth() / CardImage.WIDTH;
        int cols = image.getHeight() / CardImage.HEIGHT;
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                CardImage cardImage = cards[col * rows + row];
                File fileToSave = new File(outputFile.getAbsolutePath() + "_" + (col + 1) + "_" + (row + 1) + ".mccard");
                try {
                    HexedAces.LOGGER.info("Saving file: {}", fileToSave.getAbsoluteFile());
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    HashingOutputStream hashedOutStream = new HashingOutputStream(Hashing.sha1(), outStream);
                    cardImage.saveToStream(hashedOutStream);
                    cachedOutput.writeIfNeeded(fileToSave.toPath(), outStream.toByteArray(), hashedOutStream.hash());
                } catch (IOException e) {
                    HexedAces.LOGGER.error("Error saving file: {}", fileToSave.getAbsoluteFile(), e);
                }
            }
        }
    }

}
