package com.igrium.pseudo_pbr.pipeline;

import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Saves files to a mod folder.
 */
public interface FileConsumer {

    /**
     * Get an output stream fo a file.
     * @param relativePath File path relative to the mod root.
     * @return File output stream.
     */
    OutputStream getOutputStream(Path relativePath);
}
