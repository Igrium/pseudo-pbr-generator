package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.igrium.pseudo_pbr.pipeline.texture_sets.TextureSet.TextureType;

@Retention(RetentionPolicy.RUNTIME)
public @interface TextureField {
    String prettyName() default "";
    TextureType type() default TextureType.DEFAULT;
}
