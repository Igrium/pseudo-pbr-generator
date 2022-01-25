package com.igrium.pseudo_pbr.image_processing;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Set;

import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;

/**
 * Masks the composite to specified color channels.
 */
public class ChannelComposite implements Composite {

    Collection<ColorChannel> channels;

    /**
     * Create a composite that masks operations to specified color channels.
     * @param channels Limit to these channels.
     */
    public ChannelComposite(Collection<ColorChannel> channels) {
        this.channels = channels;
    }

    /**
     * Create a composite that masks operations to specified color channels.
     * @param channels Limit to these channels.
     */
    public ChannelComposite(ColorChannel... channels) {
        this(Set.of(channels));
    }

    private static boolean checkComponentsOrder(ColorModel cm) {
        if (cm instanceof DirectColorModel &&
                cm.getTransferType() == DataBuffer.TYPE_INT) {
            DirectColorModel directCM = (DirectColorModel) cm;
            
            return directCM.getRedMask() == 0x00FF0000 &&
                   directCM.getGreenMask() == 0x0000FF00 &&
                   directCM.getBlueMask() == 0x000000FF &&
                   (directCM.getNumComponents() != 4 ||
                    directCM.getAlphaMask() == 0xFF000000);
        }
        
        return false;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) throws RasterFormatException {
        if (!checkComponentsOrder(srcColorModel)) throw new RasterFormatException("Unsupported source color model.");
        if (!checkComponentsOrder(dstColorModel)) throw new RasterFormatException("Unsupported dest color model.");
        return new ChannelContext(channels);
    }
    
}


class ChannelContext implements CompositeContext {

    Collection<ColorChannel> channels;

    public ChannelContext(Collection<ColorChannel> channels) {
        this.channels = channels;
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        
        int[] result = new int[4];
        int[] srcPixel = new int[4];
        int[] dstPixel = new int[4];
        int[] srcPixels = new int[width];
        int[] dstPixels = new int[width];

        for (int y = 0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstPixels);
            for (int x = 0; x < width; x++) {
                int pixel = srcPixels[x];

                // pixels are stored as INT_ARGB
                // our arrays are [R, G, B, A]
                srcPixel[0] = (pixel >> 16) & 0xFF;
                srcPixel[1] = (pixel >>  8) & 0xFF;
                srcPixel[2] = (pixel      ) & 0xFF;
                srcPixel[3] = (pixel >> 24) & 0xFF;

                pixel = dstPixels[x];
                dstPixel[0] = (pixel >> 16) & 0xFF;
                dstPixel[1] = (pixel >>  8) & 0xFF;
                dstPixel[2] = (pixel      ) & 0xFF;
                dstPixel[3] = (pixel >> 24) & 0xFF;

                result[0] = channels.contains(ColorChannel.RED) ? srcPixel[0] : dstPixel[0];
                result[1] = channels.contains(ColorChannel.GREEN) ? srcPixel[1] : dstPixel[1];
                result[2] = channels.contains(ColorChannel.BLUE) ? srcPixel[2] : dstPixel[2];
                result[3] = channels.contains(ColorChannel.ALPHA) ? srcPixel[3] : dstPixel[3];

                dstPixels[x] = (result[3] & 0xFF) << 24 |
                               (result[0] & 0xFF) << 16 |
                               (result[1] & 0xFF) <<  8 |
                               result[2] & 0xFF;
            }
            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }

    @Override
    public void dispose() {        
    }
    
}
