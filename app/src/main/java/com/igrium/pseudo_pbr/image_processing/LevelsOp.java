package com.igrium.pseudo_pbr.image_processing;

import java.awt.image.BufferedImage;

/**
 * Mimics a Photoshop "levels" filter.
 */
public class LevelsOp extends LinearOp {

    private int inputLeft = 0;
    private int inputRight = 255;
    private double inputMid = 1d;

    private int outputLeft = 0;
    private int outputRight = 255;

    public LevelsOp() {};

    public LevelsOp(int inputLeft, double inputMid, int inputRight, int outputLeft, int outputRight) {
        this.inputLeft = inputLeft;
        this.inputMid = inputMid;
        this.inputRight = inputRight;
        this.outputLeft = outputLeft;
        this.outputRight = outputRight;
    }

    public int getInputLeft() {
        return inputLeft;
    }

    public void setInputLeft(int inputLeft) {
        this.inputLeft = inputLeft;
    }

    public double getInputMid() {
        return inputMid;
    }

    public void setInputMid(double inputMid) {
        this.inputMid = inputMid;
    }

    public int getInputRight() {
        return inputRight;
    }

    public void setInputRight(int inputRight) {
        this.inputRight = inputRight;
    }

    public int getOutputLeft() {
        return outputLeft;
    }

    public void setOutputLeft(int outputLeft) {
        this.outputLeft = outputLeft;
    }

    public int getOutputRight() {
        return outputRight;
    }

    public void setOutputRight(int outputRight) {
        this.outputRight = outputRight;
    }

    @Override
    public BufferedImage filter(BufferedImage source, BufferedImage dest) {
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = applyLevels((rgb >> 24) & 0xff);
                int r = applyLevels((rgb >> 16) & 0xff);
                int g = applyLevels((rgb >> 8) & 0xff);
                int b = applyLevels(rgb & 0xff);

                dest.setRGB(x, y, (
                    (a << 24) |
                    (r << 16) |
                    (g << 8) |
                    b
                ));
            }
        }
        return dest;
    }

    /**
     * Apply the levels operation to the value from a single channel.
     * 
     * @param channelValue An int from 0-255 indicating the channel's value.
     * @return An int from 0-255 indicating the transformed value.
     */
    public int applyLevels(int channelValue) {
        channelValue = (int) (255 * ((double)(channelValue - inputLeft) / (double)(inputRight - inputLeft)));
        if (inputMid != 1) {
            channelValue = applyGama(channelValue, inputMid);
        }
        channelValue = (int) ((channelValue / 225d) * (outputRight - outputLeft) + outputLeft);
        return channelValue;
    }

    // https://github.com/varunpant/GHEAT-JAVA/blob/master/JavaHeatMaps/gheat/src/main/java/gheat/graphics/GammaCorrection.java
    private int applyGama(int value, double gamma) {
        return (int) (255 * (Math.pow(value / 255d, gamma)));
    }
    
}
