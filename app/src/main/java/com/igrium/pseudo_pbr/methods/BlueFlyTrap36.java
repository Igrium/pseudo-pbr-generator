package com.igrium.pseudo_pbr.methods;

import java.awt.Graphics2D;
import java.awt.Window.Type;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.ProgressListener;
import com.igrium.pseudo_pbr.pipeline.texture_sets.SpecularGlossyTextureSet;
import com.igrium.pseudo_pbr.qc.QCFile;
import com.igrium.pseudo_pbr.qc.QCFile.QCCommand;

import com.igrium.pseudo_pbr.image_processing.BlendComposite;
import com.igrium.pseudo_pbr.image_processing.ImageUtils;
import com.igrium.pseudo_pbr.image_processing.LevelsOp;
import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;

public class BlueFlyTrap36 implements ConversionMethod<SpecularGlossyTextureSet> {

    private SpecularGlossyTextureSet textureSet = new SpecularGlossyTextureSet();
    private Path matsPath;
    private FileConsumer gameFiles;
    private FileConsumer contentFiles;

    @Override
    public SpecularGlossyTextureSet getTextureSet() {
        return textureSet;
    }

    @Override
    public void execute(File inputFile, FileConsumer gameFiles, FileConsumer contentFiles, Path enginePath,
            ProgressListener progress) throws Exception {
        this.gameFiles = gameFiles;
        this.contentFiles = contentFiles;
        
        progress.progress(0, "Initializing...");

        QCFile qc = QCFile.read(new BufferedReader(new FileReader(inputFile)));
        
        QCCommand cdMats = qc.getCommand("cdmaterials");
        if (cdMats == null) {
            matsPath = Paths.get("materials");
        } else {
            matsPath = Paths.get("materials", cdMats.getArg(0));
        }

        BufferedImage diffuse = textureSet.getDiffuse();
        if (diffuse == null) {
            throw new IllegalArgumentException("Diffuse map must be set.");
        }

        int width = diffuse.getWidth();
        int height = diffuse.getHeight();

        BufferedImage specular = textureSet.getSpecular();
        if (specular == null) {
            specular = ImageUtils.blankImage(width, height, new Color(128, 128, 128), BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage gloss = textureSet.getGloss();
        if (gloss == null) {
            gloss = ImageUtils.blankImage(width, height, Color.BLACK, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage normal = textureSet.getNormal();
        if (normal == null) {
            normal = ImageUtils.blankImage(width, height, ImageUtils.NORMAL_NEUTRAL, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage ao = textureSet.getAO();
        if (ao == null) {
            ao = ImageUtils.blankImage(width, height, Color.WHITE, BufferedImage.TYPE_INT_ARGB);
        }

        // DIFFUSE
        progress.progress(1, 6, "Generating diffuse map...");

        if (textureSet.getAO() != null) {
            Graphics2D diffuseComp = diffuse.createGraphics();
            diffuseComp.setComposite(BlendComposite.Multiply);
            diffuseComp.drawImage(textureSet.getAO(), 0, 0, null);
            diffuseComp.dispose();
        }
        writeImage("diffuse", diffuse);

        // GLOSS
        progress.progress(2, 6, "Generating exponent map...");
 
        BufferedImage exponent = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        {   
            Graphics2D comp = exponent.createGraphics();

            LevelsOp levels1 = new LevelsOp();
            levels1.setInputMid(.28);
            comp.drawImage(gloss, levels1, 0, 0);

            comp.setComposite(BlendComposite.Lighten);

            LevelsOp levels2 = new LevelsOp();
            levels2.setOutputRight(20);
            comp.drawImage(gloss, levels2, 0, 0);

            comp.dispose();
        }
        writeImage("exponent", exponent);

        // NORMAL
        progress.progress(3, 6, "Generating normal map...");

        BufferedImage normalAlpha = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        {
            Graphics2D comp = normalAlpha.createGraphics();

            LevelsOp levels1 = new LevelsOp();
            levels1.setInputMid(.24);
            comp.drawImage(gloss, levels1, 0, 0);

            comp.setComposite(BlendComposite.Lighten);

            LevelsOp levels2 = new LevelsOp();
            levels2.setOutputRight(10);
            levels2.setOutputLeft(1);
            comp.drawImage(gloss, levels2, 0, 0);

            ColorConvertOp desaturator = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
            BufferedImage tempSpec = desaturator.filter(specular, null);

            LevelsOp levels3 = new LevelsOp();
            levels3.setInputRight(10);
            levels3.filter(tempSpec, tempSpec);
            
            comp.setComposite(BlendComposite.Multiply);
            comp.drawImage(tempSpec, 0, 0, null);

            comp.dispose();
        }

        ImageUtils.applyAlpha(normalAlpha, normal, ColorChannel.RED);
        writeImage("normal", normal);

        // SPECULAR
        progress.progress(4, 6, "Generating specular map...");

        ColorConvertOp desaturator = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage specularAlpha = desaturator.filter(specular, null);
        {
            LevelsOp levels1 = new LevelsOp();
            levels1.setInputMid(.5);
            levels1.setOutputLeft(192);

            Graphics2D specComp = specularAlpha.createGraphics();
            specComp.setComposite(BlendComposite.Multiply);
            specComp.drawImage(gloss, levels1, 0, 0);

            specComp.dispose();
        }
        ImageUtils.applyAlpha(specularAlpha, specular, ColorChannel.RED);
        writeImage("specular", specular);

        // CH
        progress.progress(5, 6, "Generating CH mao...");

        BufferedImage ch = gloss; // We can now re-use the gloss buffer because we don't need the original anymore
        {
            Graphics2D chComp = ch.createGraphics();
            chComp.setComposite(BlendComposite.Multiply);
            chComp.drawImage(specularAlpha, 0, 0, null);

            chComp.dispose();
        }

        // Clear green and blue channels
        for (int x = 0; x < ch.getWidth(); x++) {
            for (int y = 0; y < ch.getHeight(); y++) {
                int rgb = ch.getRGB(x, y);
                Color color = new Color(rgb, true);
                color = new Color(color.getRed(), 0, 0, color.getAlpha());
                ch.setRGB(x, y, color.getRGB());
            }
        }

        writeImage("ch", ch);

        progress.progress(1, "Complete.");

    }


    private void writeImage(String imageName, BufferedImage image) throws IOException {
        Path imagePath = matsPath.resolve(imageName);
        OutputStream os = gameFiles.getOutputStream(imagePath);
        ImageIO.write(image, "png", os);
    }
    
}
