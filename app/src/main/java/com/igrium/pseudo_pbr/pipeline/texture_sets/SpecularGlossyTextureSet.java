package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.awt.image.BufferedImage;

public class SpecularGlossyTextureSet extends BaseTextureSet {
    
    @TextureField(prettyName = "Diffuse", type = TextureType.COLOR)
    protected BufferedImage diffuse;

    @TextureField(prettyName = "Specular", type = TextureType.SPECULAR)
    protected BufferedImage specular;

    @TextureField(prettyName = "Glossiness", type = TextureType.GLOSS)
    protected BufferedImage gloss;

    @TextureField(prettyName = "Normal", type = TextureType.NORMAL)
    protected BufferedImage normal;

    @TextureField(prettyName = "Ambient Occlusion", type = TextureType.AO)
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
