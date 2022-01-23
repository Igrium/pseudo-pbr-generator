package com.igrium.pseudo_pbr.methods;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.igrium.pseudo_pbr.image_processing.BlendComposite;
import com.igrium.pseudo_pbr.image_processing.GraphicsUtilities;
import com.igrium.pseudo_pbr.image_processing.ImageUtils;
import com.igrium.pseudo_pbr.image_processing.ImageUtils.ColorChannel;
import com.igrium.pseudo_pbr.image_processing.LevelsOp;
import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.ModelStackCreator;
import com.igrium.pseudo_pbr.pipeline.ProgressListener;
import com.igrium.pseudo_pbr.pipeline.texture_sets.SpecularGlossyTextureSet;
import com.igrium.pseudo_pbr.qc.Mesh;
import com.igrium.pseudo_pbr.qc.QCFile;
import com.igrium.pseudo_pbr.qc.QCFile.QCCommand;

public class BlueFlyTrap36 implements ConversionMethod<SpecularGlossyTextureSet> {

    private SpecularGlossyTextureSet textureSet = new SpecularGlossyTextureSet();
    private Path matsPath;
    private Path enginePath;
    private FileConsumer gameFiles;
    private FileConsumer contentFiles;

    protected static final int IMAGE_FORMAT = BufferedImage.TYPE_INT_ARGB;

    @Override
    public SpecularGlossyTextureSet getTextureSet() {
        return textureSet;
    }

    @Override
    public void execute(File inputFile, FileConsumer gameFiles, FileConsumer contentFiles, Path enginePath,
            ProgressListener progress) throws Exception {
        this.gameFiles = gameFiles;
        this.contentFiles = contentFiles;
        this.enginePath = enginePath;
        
        progress.progress(0, "Initializing...");

        QCFile qc = QCFile.read(new BufferedReader(new FileReader(inputFile)));
        
        // STACKS
        progress.progress(1, 7, "Setting up model stacking.");
        Path qcFolder = inputFile.getParentFile().toPath();
        
        // The output QC file, relative to the content root.
        Path convertedQCPath = Paths.get("modelsrc", qc.getModelName()+".qc");
        Path convertedQCFolder = convertedQCPath.getParent();

        ModelStackCreator stacker = new ModelStackCreator(name -> {
            // Mesh provider
            File file = qcFolder.resolve(name).toFile();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            Mesh mesh = Mesh.loadMesh(reader);
            reader.close();
            return mesh;

        }, (name, mesh) -> {
            // Mesh consumer
            Path filename = convertedQCFolder.resolve(name);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(contentFiles.getOutputStream(filename)));
            try {
                mesh.save(writer);
            } finally {
                writer.close();
            }
        });

        stacker.setMaterialNamingScheme((mat, index) -> {
            if (index == 0) return mat+"_base";
            if (index == 1) return mat+"_spec";
            if (index == 2) return mat+"_ch";
            if (index == 3) return mat+"_env";
            return mat+"_"+index;
        });

        stacker.setupStack(qc, 3);

        {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(contentFiles.getOutputStream(convertedQCPath)));
            qc.serialize(writer);
            writer.close();
        }

        // Mats
        QCCommand cdMats = qc.getCommand("cdmaterials");
        if (cdMats == null) {
            matsPath = Paths.get("materials");
        } else {
            matsPath = Paths.get("materials", cdMats.getArg(0));
        }

        processTextures(new ProgressListener.SubProgressListener(progress, 2, 6, 7));

    }

    // 5 steps
    private void processTextures(ProgressListener progress) throws IOException {
        BufferedImage diffuse = textureSet.getDiffuse();
        if (diffuse == null) {
            throw new IllegalArgumentException("Diffuse map must be set.");
        }
        diffuse = GraphicsUtilities.toCompatibleImage(diffuse);

        int width = diffuse.getWidth();
        int height = diffuse.getHeight();

        BufferedImage specular = textureSet.getSpecular();
        if (specular == null) {
            specular = ImageUtils.blankImage(width, height, new Color(128, 128, 128), IMAGE_FORMAT);
        } else {
            specular = GraphicsUtilities.toCompatibleImage(specular);
        }

        BufferedImage gloss = textureSet.getGloss();
        if (gloss == null) {
            gloss = ImageUtils.blankImage(width, height, Color.BLACK, IMAGE_FORMAT);
        } else {
            gloss = GraphicsUtilities.toCompatibleImage(specular);
        }

        BufferedImage normal = textureSet.getNormal();
        if (normal == null) {
            normal = ImageUtils.blankImage(width, height, ImageUtils.NORMAL_NEUTRAL, IMAGE_FORMAT);
        } else {
            normal = GraphicsUtilities.toCompatibleImage(normal);
        }

        BufferedImage ao = textureSet.getAO();
        if (ao == null) {
            ao = ImageUtils.blankImage(width, height, Color.WHITE, IMAGE_FORMAT);
        } else {
            ao = GraphicsUtilities.toCompatibleImage(ao);
        }

        // DIFFUSE
        progress.progress(0, 5, "Generating diffuse map...");
        System.out.println("Diffuse color model: " + diffuse.getColorModel());
        System.out.println("AO color type: " + ao.getType());

        if (textureSet.getAO() != null) {
            Graphics2D diffuseComp = diffuse.createGraphics();
            diffuseComp.setComposite(BlendComposite.Multiply);
            diffuseComp.drawImage(ao, 0, 0, null);
            diffuseComp.dispose();
        }
        writeImage("diffuse", diffuse);

        // GLOSS
        progress.progress(1, 5, "Generating exponent map...");

        BufferedImage exponent = GraphicsUtilities.createCompatibleImage(width, height);
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

        // Fill green and blue channels
        for (int x = 0; x < exponent.getWidth(); x++) {
            for (int y = 0; y < exponent.getHeight(); y++) {
                int rgb = exponent.getRGB(x, y);
                Color color = new Color(rgb, true);
                color = new Color(color.getRed(), 255, 255, color.getAlpha());
                exponent.setRGB(x, y, color.getRGB());
            }
        }

        writeImage("exponent", exponent);

        // NORMAL
        progress.progress(2, 5, "Generating normal map...");

        BufferedImage normalAlpha = GraphicsUtilities.createCompatibleImage(width, height);
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

            comp.setComposite(BlendComposite.Multiply);
            comp.drawImage(tempSpec, levels3, 0, 0);

            comp.dispose();
        }

        ImageUtils.applyAlpha(normalAlpha, normal, ColorChannel.RED);
        writeImage("normal", normal);

        // SPECULAR
        progress.progress(3, 5, "Generating specular map...");

        ColorConvertOp desaturator = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage specularAlpha = GraphicsUtilities.toCompatibleImage(desaturator.filter(specular, null));
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
        progress.progress(4, 5, "Generating CH map...");

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
        Path imagePath = matsPath.resolve(imageName+".png");
        OutputStream os = gameFiles.getOutputStream(imagePath);
        ImageIO.write(image, "png", os);
    }
    
}
