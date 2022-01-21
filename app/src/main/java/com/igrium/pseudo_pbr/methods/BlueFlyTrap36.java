package com.igrium.pseudo_pbr.methods;

import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.ProgressListener;
import com.igrium.pseudo_pbr.pipeline.texture_sets.SpecularGlossyTextureSet;
import com.igrium.pseudo_pbr.qc.QCFile;
import com.igrium.pseudo_pbr.qc.QCFile.QCCommand;

public class BlueFlyTrap36 implements ConversionMethod<SpecularGlossyTextureSet> {

    private SpecularGlossyTextureSet textureSet = new SpecularGlossyTextureSet();

    @Override
    public SpecularGlossyTextureSet getTextureSet() {
        return textureSet;
    }

    @Override
    public void execute(File inputFile, FileConsumer gameFiles, FileConsumer contentFiles, Path enginePath,
            ProgressListener progress) throws Exception {
        QCFile qc = QCFile.read(new BufferedReader(new FileReader(inputFile)));
        
        QCCommand cdMats = qc.getCommand("cdmaterials");
        Path matsPath;
        if (cdMats == null) {
            matsPath = Paths.get("materials");
        } else {
            matsPath = Paths.get("materials", cdMats.getArg(0));
        }

        progress.progress(1f / 5f, "Writing diffuse texture.");
        writeImage(matsPath, "diffuse.png", gameFiles, textureSet.getDiffuse());
        progress.progress(2f / 5f, "Writing specular texture.");
        writeImage(matsPath, "spec.png", gameFiles, textureSet.getSpecular());
        progress.progress(3f / 5f, "Writing glossiness texture.");
        writeImage(matsPath, "gloss.png", gameFiles, textureSet.getGloss());
        progress.progress(4f / 5f, "Writing normal texture.");
        writeImage(matsPath, "normal.png", gameFiles, textureSet.getNormal());
        progress.progress(1, "Complete.");
        
    }

    private void writeImage(Path matsPath, String imageName, FileConsumer consumer, BufferedImage image) throws IOException {
        Path imagePath = matsPath.resolve(imageName);
        OutputStream os = consumer.getOutputStream(imagePath);
        ImageIO.write(image, "png", os);
    }
    
}
