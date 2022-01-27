package com.igrium.pseudo_pbr.image_processing;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;

import java.awt.image.WritableRaster;

/**
 * Copies a grayscale version of a <code>BufferedImage</code> into the alpha
 * channel of the subject.
 */
public class ApplyAlphaFilter extends LinearOp {

    BufferedImage sourceImage;

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * Create a filter that copies a grayscale version of a
     * <code>BufferedImage</code> into the alpha channel of the target.
     * 
     * @param sourceImage Image to copy from. RGB channels are averaged to form
     *                    grayscale source,
     */
    public ApplyAlphaFilter(BufferedImage sourceImage) {
        this.sourceImage = new ColorConvertOp(
                sourceImage.getColorModel().getColorSpace(),
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
            .filter(sourceImage, null);        
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {

        if (dest == null) {
            dest = createCompatibleDestImage(src, null);
        }
        
        Graphics2D comp = dest.createGraphics();
        comp.drawImage(src, 0, 0, null);
        comp.dispose();

        WritableRaster alpha = dest.getAlphaRaster();
        alpha.setDataElements(0, 0, sourceImage.getRaster());
        
        return dest;
    }
    
}
