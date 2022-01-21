package com.igrium.pseudo_pbr.pipeline;

import com.igrium.pseudo_pbr.pipeline.texture_sets.TextureSet;

public interface ConversionMethod<T extends TextureSet> {

    /**
     * Get the texture set that this conversion method wants to use. Place the
     * desired textures in this texture set.
     * 
     * @return Active texture set.
     */
    public T getTextureSet();
    
    /**
     * Execute the conversion.
     * @param gameFiles File consumer to take files that go in the <code>game</code> directory.
     * @param contentFiles File consumer to take files that go in the <code>content</code> directory.
     * @throws Exception If anything goes wrong in the process.
     */
    public void execute(FileConsumer gameFiles, FileConsumer contentFiles) throws Exception;
}
