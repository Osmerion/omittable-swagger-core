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
import com.osmerion.omittable.jackson.OmittableModule;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
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
public final class OmittableModelConverter extends ModelResolver {

    /**
     * Creates a new {@link OmittableModelConverter}.
     *
     * @param mapper    the mapper to use as template for the converter. The actual mapper is derived from the given
     *                  mapper by copying it and registering the {@link OmittableModule}.
     *
     * @since   0.2.0
     */
    public OmittableModelConverter(ObjectMapper mapper) {
        super(mapper.copy().registerModule(new OmittableModule()));
    }

    @Override
    public @Nullable Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Schema<?> schema = super.resolve(annotatedType, context, chain);

        if (schema != null && "object".equals(schema.getType()) && schema.getProperties() != null) {
            JavaType type = _mapper.constructType(annotatedType.getType());
            BeanDescription beanDesc = _mapper.getSerializationConfig().introspect(type);
            List<BeanPropertyDefinition> properties = beanDesc.findProperties();

            for (BeanPropertyDefinition prop : properties) {
                JavaType propType = prop.getPrimaryMember().getType();

                if (!propType.hasRawClass(Omittable.class)) {
                    // If it's NOT Omittable, mark it as required
                    addRequiredItem(schema, prop.getName());
                } else {
                    List<String> required = schema.getRequired();
                    if (required != null && required.stream().anyMatch(it -> it.equals(prop.getName()))) {
                        throw new IllegalArgumentException("Omittable property may not be marked as required");
                    }
                }
            }
        }

        return schema;
    }

}
