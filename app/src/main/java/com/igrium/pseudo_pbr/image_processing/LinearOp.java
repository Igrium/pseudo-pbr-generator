package com.igrium.pseudo_pbr.image_processing;

import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.HashMap;

/**
 * A buffered image op that doesn't change pixel count.
 */
public abstract class LinearOp implements BufferedImageOp {

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage img, ColorModel colorModel) {
        BufferedImage image = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        return image;
    }

    @Override
    public Rectangle getBounds2D(BufferedImage image) {
        return new Rectangle(image.getWidth(), image.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt != null) {
            dstPt.setLocation(srcPt);
            return dstPt;
        } else {
            return srcPt;
        }
    }

    @Override
    public RenderingHints getRenderingHints() {
        return new RenderingHints(new HashMap<>());
    }
    
}
