package com.capitalone.controller.httpm;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.HttpMethod;

/**
 * Allows support for the HTTP REST method as an annotation, @PATCH
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
public @interface PATCH {
	
}
