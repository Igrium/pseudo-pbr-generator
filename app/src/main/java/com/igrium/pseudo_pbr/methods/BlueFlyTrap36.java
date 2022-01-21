package com.igrium.pseudo_pbr.methods;

import com.igrium.pseudo_pbr.pipeline.ConversionMethod;
import com.igrium.pseudo_pbr.pipeline.FileConsumer;
import com.igrium.pseudo_pbr.pipeline.texture_sets.SpecularGlossyTextureSet;

public class BlueFlyTrap36 implements ConversionMethod<SpecularGlossyTextureSet> {

    @Override
    public SpecularGlossyTextureSet getTextureSet() {
        return new SpecularGlossyTextureSet();
    }

    @Override
    public void execute(FileConsumer gameFiles, FileConsumer contentFiles) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
}
