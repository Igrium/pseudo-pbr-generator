package com.igrium.pseudo_pbr.image_processing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public final class ImageUtils {
    private ImageUtils() {};

    /**
     * The classic purple color of a normal map with no detail.
     */
    public static final Color NORMAL_NEUTRAL = new Color(128, 128, 255);

    public enum ColorChannel { RED, GREEN, BLUE, ALPHA }

    /**
     * Clone a buffered image.
     * @param image Image to clone.
     * @return Copy of image.
     */
    public static BufferedImage cloneImage(BufferedImage image) {
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(image.getColorModel(), raster, image.isAlphaPremultiplied(), null);
    }

    /**
     * Construct a buffered image pre-filled with a set color.
     * @param width Image width.
     * @param height Image height.
     * @param color Color to fill with.
     * @param type Image type.
     * @return The image.
     */
    public static BufferedImage blankImage(int width, int height, Color color, int type) {
        BufferedImage image = new BufferedImage(width, height, type);
        Graphics2D gfx = image.createGraphics();
        gfx.setColor(color);
        gfx.fillRect(0, 0, width, height);
        gfx.dispose();
        return image;
    } 
    
    /**
     * Copy a channel from one image into the alpha channel of another image.
     * @param source Source image.
     * @param dest Destination image.
     * @param channel Channel from source image.
     * @return <code>dest</code>
     */
    public static BufferedImage applyAlpha(BufferedImage source, BufferedImage dest, ColorChannel channel) {
        if (source.getWidth() != dest.getWidth() || source.getHeight() != dest.getHeight()) {
            throw new IllegalArgumentException("Source and destination images must be the same size!");
        }

        for (int x = 0; x < dest.getWidth(); x++) {
            for (int y = 0; y < dest.getHeight(); y++) {
                int sourceRGB = source.getRGB(x, y);
                int destRGB = dest.getRGB(x, y);
                
                int val;
                if (channel == ColorChannel.RED) {
                    val = (sourceRGB >> 16) & 0xff;
                } else if (channel == ColorChannel.GREEN) {
                    val = (sourceRGB >> 8) & 0xff;
                } else if (channel == ColorChannel.BLUE) {
                    val = (sourceRGB >> 0) & 0xff;
                } else {
                    val = (sourceRGB >> 24) & 0xff;
                }

                int mc = (val << 24) | 0x00ffffff;
                dest.setRGB(x, y, destRGB & mc);
            }
        }

        return dest;
    }
}
