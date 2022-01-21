package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseTextureSet implements TextureSet {

    private Map<String, Field> fields = new HashMap<>();

    public BaseTextureSet() {
        setupReflection(this.getClass());
    }

    private void setupReflection(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(TextureField.class)) continue;
            if (!BufferedImage.class.isAssignableFrom(field.getType())) {
                throw new AssertionError("Field "+field.getName()+" must be assignable to BufferedImage!");
            }
            field.setAccessible(true);
            TextureField annotation = field.getAnnotation(TextureField.class);
            String name = annotation.prettyName();
            if (name == null || name.length() == 0) {
                name = field.getName();
            }
            fields.put(name, field);
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass.equals(Object.class)) return;
        setupReflection(superclass);
    }

    @Override
    public Set<String> getTextureMapNames() {
        return fields.keySet();
    }

    @Override
    public Map<String, BufferedImage> getTextureMaps() {
        Map<String, BufferedImage> map = new HashMap<>();
        try {
            for (String name : fields.keySet()) {
                map.put(name, (BufferedImage) fields.get(name).get(this));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access field in texture set.", e);
        }  
        return map;
    }

    @Override
    public void setTextureMaps(Map<String, BufferedImage> textureMaps) {
        try {
            for (String name : textureMaps.keySet()) {
                Field field = fields.get(name);
                if (field == null) continue;
                field.set(this, textureMaps.get(name));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to set field value.", e);
        }
        
    }
    
}
