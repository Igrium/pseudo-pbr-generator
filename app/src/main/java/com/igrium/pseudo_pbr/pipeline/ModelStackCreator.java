package com.igrium.pseudo_pbr.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.igrium.pseudo_pbr.qc.Mesh;
import com.igrium.pseudo_pbr.qc.QCFile;
import com.igrium.pseudo_pbr.qc.QCFile.QCCommand;

/**
 * Handles the stacking of models.
 */
public class ModelStackCreator {
    public static interface MeshProvider {
        public Mesh get(String name) throws IOException;
    }

    public static interface MeshConsumer {
        public void accept(String name, Mesh mesh) throws IOException;
    }

    public static interface MaterialNamingScheme {
        public String apply(String name, int index);
    }

    protected MeshProvider meshProvider;
    protected MeshConsumer meshConsumer;
    protected MaterialNamingScheme materialNamingScheme = (name, index) -> {
        return name+"_"+index;
    };

    /**
     * Create a model stack creator.
     * @param meshProvider A function that loads mesh files based on their QC name.
     * @param meshConsumer A function that saves mesh files based on their QC name.
     */
    public ModelStackCreator(MeshProvider meshProvider, MeshConsumer meshConsumer) {
        this.meshProvider = meshProvider;
        this.meshConsumer = meshConsumer;
    }

    /**
     * Setup stacking on a model.
     * @param file Model's QC file. Will be modified.
     * @param numStacks Number of stacks to set.
     * @return A mapping of base mesh names and the materials in their stacks.
     */
    public Map<String, List<String>> setupStack(QCFile file, int numStacks) {
        List<QCCommand> modelCommands = new ArrayList<>();

        for (QCCommand command : file.getCommands()) {
            if (command.getName().equals("model") || command.getName().equals("body")) {
                modelCommands.add(command);
            }
        }

        Map<String, List<String>> mats = new HashMap<>();
        for (QCCommand command : modelCommands) {
            String meshName = command.getArg(1);
            Mesh mesh;
            try {
                mesh = meshProvider.get(meshName);
            } catch (IOException e) {
                System.err.println("Unable to load mesh: "+meshName);
                e.printStackTrace();
                continue;
            }
            file.getCommands().remove(command);
            List<String> stackMats = new ArrayList<>();
            for (int i = 0; i < numStacks; i++) {
                String newName = remapMeshName(meshName, i);
                Mesh newMesh = mesh.clone();
                
                Set<String> oldMatNames = new HashSet<>();
                oldMatNames.addAll(newMesh.getMatNames());
                if (oldMatNames.size() > 1) {
                    throw new IllegalArgumentException("Pseudo PBR Generator only works with single-material models for now.");
                }
                for (String mat : oldMatNames) {
                    String newMatName = materialNamingScheme.apply(mat, i);
                    newMesh.changeMatName(mat, newMatName);
                    stackMats.add(newMatName);
                }

                try {
                    meshConsumer.accept(newName, newMesh);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to save mesh file "+newName, e);
                }
                
                file.getCommands().add(new QCCommand("model", command.getArg(0), newName));
            }
            mats.put(meshName, stackMats);
        }
        return mats;
    }

    private String remapMeshName(String name, int index) {
        if (index == 0) return name;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0) return name+"_"+index;
        return name.substring(0, dotIndex)+"_"+index+name.substring(dotIndex);
    }
    
    /**
     * Get the name of a material on a certian stack index.
     * @param name Material name.
     * @param index Stack index.
     * @return Updated material name.
     */
    public String getMaterialName(String name, int index) {
        return materialNamingScheme.apply(name, index);
    }

    /**
     * Set the stack's material naming scheme.
     * @param materialNamingScheme Name remapping function.
     */
    public void setMaterialNamingScheme(MaterialNamingScheme materialNamingScheme) {
        this.materialNamingScheme = materialNamingScheme;
    }
    
}
