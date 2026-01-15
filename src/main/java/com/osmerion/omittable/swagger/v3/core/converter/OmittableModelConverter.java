/*
 * Copyright 2025-2026 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.osmerion.omittable.swagger.v3.core.converter;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.osmerion.omittable.Omittable;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A {@link ModelConverter} that handles the {@link Omittable} type.
 *
 * <p>The converter marks non-omittable properties as required.</p>
 *
 * @since   0.2.0
 *
 * @author  Leon Linhart
 */
public final class OmittableModelConverter implements ModelConverter {

    private final ObjectMapper mapper;

    /**
     * Creates a new {@link OmittableModelConverter}.
     *
     * @param mapper    the mapper to use as template for the converter
     *
     * @since   0.2.0
     */
    public OmittableModelConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public @Nullable Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        // See https://github.com/springdoc/springdoc-openapi/issues/712#issuecomment-639062595
        JavaType javaType = this.mapper.constructType(annotatedType.getType());
        Class<?> cls = javaType.getRawClass();

        if (cls != null && Omittable.class.isAssignableFrom(cls)) {
            JavaType innerType = javaType.getBindings().getBoundType(0);
            if (innerType == null) {
                innerType = this.mapper.constructType(Object.class);
            }

            annotatedType = new AnnotatedType()
                .type(innerType)
                .name(annotatedType.getName())
                .parent(annotatedType.getParent())
                .skipOverride(true)
                .schemaProperty(annotatedType.isSchemaProperty())
                .ctxAnnotations(annotatedType.getCtxAnnotations())
                .resolveAsRef(annotatedType.isResolveAsRef())
                .resolveEnumAsRef(annotatedType.isResolveEnumAsRef())
                .jsonViewAnnotation(annotatedType.getJsonViewAnnotation())
                .propertyName(annotatedType.getPropertyName())
                .subtype(annotatedType.isSubtype());

            return this.resolve(annotatedType, context, chain);
        }

        Schema<?> schema = null;
        if (chain.hasNext()) {
            schema = chain.next().resolve(annotatedType, context, chain);
        }

        if (schema != null) {
            Schema<?> objectSchema = schema;

            if (schema.get$ref() != null) {
                objectSchema = context.resolve(annotatedType);
            }

            if (objectSchema.getProperties() != null && !objectSchema.getProperties().isEmpty()) {
                this.calculateRequiredProperties(objectSchema, javaType);
            }
        }

        return schema;
    }

    private void calculateRequiredProperties(Schema<?> schema, JavaType javaType) {
        BeanDescription beanDesc = this.mapper.getSerializationConfig().introspect(javaType);
        List<BeanPropertyDefinition> properties = beanDesc.findProperties();

        for (BeanPropertyDefinition prop : properties) {
            JavaType propType = prop.getPrimaryMember().getType();

            if (!Omittable.class.isAssignableFrom(propType.getRawClass())) {
                String schemaPropertyName = prop.getName();
                this.addRequiredItem(schema, schemaPropertyName);
            } else {
                List<String> required = schema.getRequired();
                if (required != null && required.stream().anyMatch(it -> it.equals(prop.getName()))) {
                    throw new IllegalArgumentException("Omittable property may not be marked as required");
                }
            }
        }
    }

    private void addRequiredItem(Schema<?> schema, String propertyName) {
        List<String> required = schema.getRequired();
        if (required == null || !required.contains(propertyName)) {
            schema.addRequiredItem(propertyName);
        }
    }

}
