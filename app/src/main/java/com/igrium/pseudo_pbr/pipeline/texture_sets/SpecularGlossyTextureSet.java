package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.awt.image.BufferedImage;

public class SpecularGlossyTextureSet extends BaseTextureSet {
    
    @TextureField(prettyName = "Diffuse")
    protected BufferedImage diffuse;

    @TextureField(prettyName = "Specular")
    protected BufferedImage specular;

    @TextureField(prettyName = "Glossiness")
    protected BufferedImage gloss;

    @TextureField(prettyName = "Normal")
    protected BufferedImage normal;

    @TextureField(prettyName = "Ambient Occlusion")
    protected BufferedImage ao;

    public BufferedImage getDiffuse() {
        return diffuse;
    }

    public BufferedImage getSpecular() {
        return specular;
    }

    public BufferedImage getGloss() {
        return gloss;
    }

    public BufferedImage getAO() {
        return ao;
    }

    public BufferedImage getNormal() {
        return normal;
    }
}
