package com.igrium.pseudo_pbr.image_processing;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;

/**
 * Copies a channel from a <code>BufferedImage</code> into a channel of the subject.
 */
public class ApplyChannelFilter extends LinearOp {

    BufferedImage sourceImage;
    ColorChannel srcChannel;
    ColorChannel dstChannel;

    /**
     * Create a filter that copies a channel from a <code>BufferedImage</code> into
     * a channel of the target.
     * 
     * @param sourceImage Image to copy from.
     * @param srcChannel  The channel to read from.
     * @param dstChannel  The channel to copy into.
     */
    public ApplyChannelFilter(BufferedImage sourceImage, ColorChannel srcChannel, ColorChannel dstChannel) {
        this.sourceImage = sourceImage;
        this.srcChannel = srcChannel;
        this.dstChannel = dstChannel;
    }

    @Override
    public BufferedImage filter(BufferedImage target, BufferedImage dest) {
        if (target.getWidth() != sourceImage.getWidth() || target.getHeight() != sourceImage.getHeight()) {
            throw new IllegalArgumentException("Target image must be the same size as the channel source image.");
        }

        if (dest == null) dest = createCompatibleDestImage(target, ColorModel.getRGBdefault());

        for (int y = 0; y < dest.getHeight(); y++) {
            for (int x = 0; x < dest.getWidth(); x++) {
                int val;

                int rgb = target.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                rgb = sourceImage.getRGB(x, y);
                int sa = (rgb >> 24) & 0xff;
                int sr = (rgb >> 16) & 0xff;
                int sg = (rgb >> 8) & 0xff;
                int sb = rgb & 0xff;

                switch (srcChannel) {
                    case RED:
                        val = sr;
                        break;
                    case GREEN:
                        val = sg;
                        break;
                    case BLUE:
                        val = sb;
                        break;
                    default:
                        val = sa; // Default to alpha
                        break;
                }

                switch (dstChannel) {
                    case RED:
                        r = val;
                        break;
                    case GREEN:
                        g = val;
                        break;
                    case BLUE:
                        b = val;
                    case ALPHA:
                        a = val;
                }

                dest.setRGB(x, y, (
                    (a << 24) |
                    (r << 16) |
                    (g << 8)  |
                    b
                ));
            }
        }
        return dest;
    }
    
}
