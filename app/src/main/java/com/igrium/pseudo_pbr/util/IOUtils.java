package com.igrium.pseudo_pbr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class IOUtils {
    private IOUtils() {};

    public static String readFile(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            builder.append(currentLine).append(System.lineSeparator());
        }

        return builder.toString();
    }
}
