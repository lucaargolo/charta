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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CardImageUtils {

    public static SuitImage EMPTY_SUIT = new SuitImage();
    public static StreamCodec<ByteBuf, SuitImage> SUIT_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, SuitImage::compress, SuitImage::decompress);

    public static CardImage EMPTY_CARD = new CardImage();
    public static StreamCodec<ByteBuf, CardImage> CARD_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, CardImage::compress, CardImage::decompress);

    static {
        for(int x = 0; x < SuitImage.WIDTH; x++) {
            for(int y = 0; y < SuitImage.HEIGHT; y++) {
                EMPTY_SUIT.setPixel(x, y, x > SuitImage.WIDTH/2 ? y > SuitImage.HEIGHT/2 ? 0 : 58 : y > SuitImage.HEIGHT/2 ? 58 : 0, 3);
            }
        }
        for(int x = 0; x < CardImage.WIDTH; x++) {
            for(int y = 0; y < CardImage.HEIGHT; y++) {
                EMPTY_CARD.setPixel(x, y, x > CardImage.WIDTH/2 ? y > CardImage.HEIGHT/2 ? 0 : 58 : y > CardImage.HEIGHT/2 ? 58 : 0, 3);
            }
        }
    }

    public static <I extends CardImage> DynamicTexture convertImage(I image) {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, image.getWidth(), image.getHeight(), true);
        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                nativeImage.setPixelLuminance(x, y, image.getPixel(x, y));
            }
        }
        return new DynamicTexture(nativeImage);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static <I extends CardImage> void saveImage(I image, File fileToSave, CachedOutput cachedOutput) {
        try {
            Charta.LOGGER.info("Saving file: {}", fileToSave.getAbsoluteFile());
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            HashingOutputStream hashedOutStream = new HashingOutputStream(Hashing.sha1(), outStream);
            image.saveToStream(hashedOutStream);
            cachedOutput.writeIfNeeded(fileToSave.toPath(), outStream.toByteArray(), hashedOutStream.hash());
        } catch (IOException e) {
            Charta.LOGGER.error("Error saving file: {}", fileToSave.getAbsoluteFile(), e);
        }
    }

}
