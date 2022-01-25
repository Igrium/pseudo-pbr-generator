package com.igrium.pseudo_pbr.image_processing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;

/**
 * Extracts a color channel into all three color channels of a buffered image.
 */
public class ExtractChannelOp extends LinearOp {
    private ColorChannel colorChannel;

    /**
     * Extracts a color channel into all three color channels of a buffered image.
     * @param channel Color channel to extract.
     */
    public ExtractChannelOp(ColorChannel channel) {
        this.colorChannel = channel;
    }

    @Override
    public BufferedImage filter(BufferedImage source, BufferedImage dest) {
        if (dest == null) dest = createCompatibleDestImage(source, ColorModel.getRGBdefault());
        
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int val;
                if (colorChannel == ColorChannel.RED) {
                    val = r;
                } else if (colorChannel == ColorChannel.GREEN) {
                    val = g;
                } else if (colorChannel == ColorChannel.BLUE) {
                    val = b;
                } else {
                    val = a;
                }

                dest.setRGB(x, y, new Color(val, val, val, val).getRGB());
            }
        }

        return dest;
    }
    
}
