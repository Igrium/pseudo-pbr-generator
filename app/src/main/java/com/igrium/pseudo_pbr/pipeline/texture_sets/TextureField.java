package com.igrium.pseudo_pbr.pipeline.texture_sets;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TextureField {
    String prettyName() default "";
}
