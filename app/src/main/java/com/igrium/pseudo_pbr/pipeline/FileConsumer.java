package com.igrium.pseudo_pbr.pipeline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Saves files to a mod folder.
 */
public interface FileConsumer {

    /**
     * Get an output stream of a file.
     * @param relativePath File path relative to the mod root.
     * @return File output stream.
     * @exception IOException If an IO exception occurs while opening the output stream.
     */
    OutputStream getOutputStream(Path relativePath) throws IOException;

    /**
     * A file consumer that outputs files into a folder.
     */
    public class BasicFileConsumer implements FileConsumer {

        private Path base;
        
        /**
         * Construct a basic file consumer.
         * @param base Mod (or content) root path.
         */
        public BasicFileConsumer(Path base) {
            this.base = base;
        }

        public Path getBase() {
            return base;
        }

        @Override
        public OutputStream getOutputStream(Path relativePath) throws IOException {
            File file = base.resolve(relativePath).toFile();
            File parent = file.getParentFile();
            if (!parent.isDirectory()) {
                parent.mkdirs();
            }
            return new FileOutputStream(file);
        }
        
    }
}
