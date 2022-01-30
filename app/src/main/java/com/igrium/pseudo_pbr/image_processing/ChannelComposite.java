package com.igrium.pseudo_pbr.image_processing;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Set;

import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;

/**
 * Masks the composite to specified color channels.
 * @deprecated Doesn't work yet.
 */
@Deprecated
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

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new ChannelContext(channels, srcColorModel, dstColorModel);
    }
    
}

class ChannelContext implements CompositeContext {

    Collection<ColorChannel> channels;
    ColorModel srcColorModel;
    ColorModel dstColorModel;

    public ChannelContext(Collection<ColorChannel> channels, ColorModel srcColorModel, ColorModel dstColorModel) {
        this.channels = channels;
        this.srcColorModel = srcColorModel;
        this.dstColorModel = dstColorModel;
    }

    @Override
    public void dispose() {}

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());

        boolean alpha = channels.contains(ColorChannel.ALPHA);
        boolean red = channels.contains(ColorChannel.RED);
        boolean green = channels.contains(ColorChannel.GREEN);
        boolean blue = channels.contains(ColorChannel.BLUE);
        
        Object srcBuffer = src.getDataElements(0, 0, null);
        Object destBuffer = dstIn.getDataElements(0, 0, null);

        // ARGB
        int[] pixelCache = new int[4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                destBuffer = dstIn.getDataElements(x, y, destBuffer);
                pixelCache[0] = dstColorModel.getAlpha(destBuffer);
                pixelCache[1] = dstColorModel.getRed(destBuffer);
                pixelCache[2] = dstColorModel.getGreen(destBuffer);
                pixelCache[3] = dstColorModel.getBlue(destBuffer);

                srcBuffer = src.getDataElements(x, y, srcBuffer);

                if (alpha) pixelCache[0] = srcColorModel.getAlpha(srcBuffer);
                if (red) pixelCache[1] = srcColorModel.getRed(srcBuffer);
                if (green) pixelCache[2] = srcColorModel.getGreen(srcBuffer);
                if (blue) pixelCache[3] = srcColorModel.getBlue(srcBuffer);

                destBuffer = dstColorModel.getDataElements(new Color(
                        pixelCache[1],
                        pixelCache[2],
                        pixelCache[3],
                        pixelCache[0]
                    ).getRGB(), destBuffer);
                
                dstOut.setDataElements(x, y, destBuffer);
            }
        }
    }
}