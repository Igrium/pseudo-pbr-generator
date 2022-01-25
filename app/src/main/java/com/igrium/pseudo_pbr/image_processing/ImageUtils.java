package com.igrium.pseudo_pbr.image_processing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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
        BufferedImage image = GraphicsUtilities.createCompatibleImage(width, height);
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
                Color sourceRGB = new Color(source.getRGB(x, y));
                Color destRGB = new Color(dest.getRGB(x, y));
                
                int val;
                if (channel == ColorChannel.RED) {
                    val = sourceRGB.getRed();
                } else if (channel == ColorChannel.GREEN) {
                    val = sourceRGB.getGreen();
                } else if (channel == ColorChannel.BLUE) {
                    val = sourceRGB.getBlue();
                } else {
                    val = sourceRGB.getAlpha();
                }

                dest.setRGB(x, y, new Color(destRGB.getRed(), destRGB.getGreen(), destRGB.getBlue(), val).getRGB());
            }
        }

        return dest;
    }

    public static BufferedImage desaturate(BufferedImage image) {
        ColorConvertOp desaturator = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return GraphicsUtilities.toCompatibleImage(desaturator.filter(image, null));
    }
}
