package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;


/**
 * Represents a set of base textures.
 */
public interface TextureSet {

    public enum TextureType {
        DEFAULT, COLOR, NORMAL, SPECULAR, ROUGHNESS, GLOSS, AO
    }

    /**
     * Get a set of texture map names that this texture set takes.
     * @return Texture map names.
     */
    Set<String> getTextureMapNames();

    /**
     * Get all assigned texture maps.
     */
    Map<String, BufferedImage> getTextureMaps();

    /**
     * Set all texture maps. Absent values will be left as-is.
     */
    void setTextureMaps(Map<String, BufferedImage> textureMaps);

    /**
     * Get the type of a particular texture.
     * @param texID Texture ID.
     * @return The texture type.
     * @throws IllegalArgumentException If the texture set does not have a texture with this ID.
     */
    TextureType getTextureType(String texID) throws IllegalArgumentException;
}
