package com.igrium.pseudo_pbr.qc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A light representation of source engine SMD files.
 * Only processes enough to change material names.
 */
public class Mesh implements Cloneable {
    private List<String> header;
    private List<String> triangles;
    private Set<String> matNames;

    public String getHeader() {
        return String.join(System.lineSeparator(), header);
    }

    public String getTriangles() {
        return String.join(System.lineSeparator(), triangles);
    }

    public Set<String> getMatNames() {
        return matNames;
    }

    protected Mesh() {};

    protected void genMatNames() {
        matNames = new HashSet<>();
        for (int i = 0; i < triangles.size() - 1; i += 4) {
            matNames.add(triangles.get(i).strip());
        }
    }

    public boolean changeMatName(String oldName, String newName) {
        if (!matNames.remove(oldName)) return false;

        for (int i = 0; i < triangles.size(); i ++) {
            if (triangles.get(i).strip().equals(oldName)) {
                triangles.set(i, newName);
            }
        }
        matNames.add(newName);

        return true;
    }

    /**
     * Save this mesh to a file.
     * @param writer Writer to save to.
     * @throws IOException
     */
    public void save(Writer writer) throws IOException {
        writer.write(getHeader());
        writer.write(System.lineSeparator());
        writer.write(getTriangles());
    }

    /**
     * Read the reader until a target line is found.
     * @param reader Reader to use.
     * @param target Target string.
     * @return List of lines that were read, inclusive of target line.
     * @throws IOException
     */
    protected static List<String> readUntil(BufferedReader reader, String target) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = null;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                throw new EOFException("End of file hit before '"+target+"' was found.");
            }
            if (target.equals(line.strip())) {
                lines.add(line);
                return lines;
            } else {
                lines.add(line);
            }
        }
    }
    
    public static Mesh loadMesh(BufferedReader reader) throws IOException {
        Mesh mesh = new Mesh();
        mesh.header = readUntil(reader, "triangles");
        mesh.triangles = readUntil(reader, "end");
        mesh.genMatNames();

        return mesh;
    }

    @Override
    public Mesh clone() {
        Mesh other = new Mesh();
        other.header = new ArrayList<>();
        other.header.addAll(this.header);

        other.triangles = new ArrayList<>();
        other.triangles.addAll(this.triangles);

        other.matNames = new HashSet<>();
        other.matNames.addAll(this.matNames);

        return other;
    }
}
