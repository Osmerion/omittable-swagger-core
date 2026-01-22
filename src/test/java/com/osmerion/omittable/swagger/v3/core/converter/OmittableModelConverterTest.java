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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.osmerion.omittable.Omittable;
import com.osmerion.omittable.jackson.OmittableModule;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

public final class OmittableModelConverterTest {

    @Test
    void testModelConverter() {
        ModelConverters converters = new ModelConverters();
        converters.addConverter(new OmittableModelConverter(new ObjectMapper()));

        assertThat(converters.read(PersonUpdate.class))
            .hasSize(1)
            .extractingByKey("PersonUpdate")
            .satisfies(
                schema -> assertThat(schema.getType())
                    .isEqualTo("object"),
                schema -> assertThat(schema)
                    .extracting(Schema::getRequired)
                    .asInstanceOf(LIST)
                    .containsExactly("required", "requiredNullable"),
                schema -> assertThat(schema)
                    .extracting(Schema::getProperties)
                    .asInstanceOf(InstanceOfAssertFactories.map(String.class, Schema.class))
                    .containsOnlyKeys("name", "required", "requiredNullable", "nullable")
                    .hasEntrySatisfying(
                        "name",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "required",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "requiredNullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "nullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
            );
    }

    @Test
    void testModelConverter_Nested() {
        ModelConverters converters = new ModelConverters();
        converters.addConverter(new OmittableModelConverter(new ObjectMapper()));

        JavaType type = TypeFactory.defaultInstance().constructParametricType(Omittable.class, PersonUpdate.class);

        assertThat(converters.read(type))
            .hasSize(1)
            .extractingByKey("PersonUpdate")
            .satisfies(
                schema -> assertThat(schema.getType())
                    .isEqualTo("object"),
                schema -> assertThat(schema)
                    .extracting(Schema::getRequired)
                    .asInstanceOf(LIST)
                    .containsExactly("required", "requiredNullable"),
                schema -> assertThat(schema)
                    .extracting(Schema::getProperties)
                    .asInstanceOf(InstanceOfAssertFactories.map(String.class, Schema.class))
                    .containsOnlyKeys("name", "required", "requiredNullable", "nullable")
                    .hasEntrySatisfying(
                        "name",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "required",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "requiredNullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
                    .hasEntrySatisfying(
                        "nullable",
                        property -> assertThat(property)
                            .satisfies(
                                it -> assertThat(it).extracting(Schema::getType).isEqualTo("string"),
                                it -> assertThat(it).extracting(Schema::getNullable).isNull()
                            )
                    )
            );
    }

    record PersonUpdate(
        Omittable<String> name,
        String required,
        @Nullable String requiredNullable,
        Omittable<@Nullable String> nullable
    ) {}

    @Test
    void testModelConverter_OmittableRequired() {
        ModelConverters converters = new ModelConverters();
        converters.addConverter(new OmittableModelConverter(new ObjectMapper()));

        assertThatThrownBy(() -> converters.read(PersonUpdateOmittableRequired.class))
            .isInstanceOf(IllegalArgumentException.class);
    }

    record PersonUpdateOmittableRequired(
        @io.swagger.v3.oas.annotations.media.Schema(requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
        Omittable<String> name
    ) {}

    @Test
    void testModelConverter_NestedWithSchemaAnnotation() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(MyString.class, new MyStringSerializer(null));
        module.addDeserializer(MyString.class, new MyStringDeserializer(null));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OmittableModule());
        objectMapper.registerModule(module);

        ModelConverters converters = new ModelConverters();
        converters.addConverter(new OmittableModelConverter(objectMapper));

        record Container(String a, Omittable<MyString> b) {}

        assertThat(converters.read(Container.class))
            .hasSize(1)
            .extractingByKey("Container")
            .satisfies(
                schema -> assertThat(schema.getType())
                    .isEqualTo("object"),
                schema -> assertThat(schema)
                    .extracting(Schema::getRequired)
                    .asInstanceOf(LIST)
                    .containsExactly("a"),
                schema -> assertThat(((Schema<?>) schema).getProperties())
                    .containsOnlyKeys("a", "b")
                    .extractingByKey("b")
                    .satisfies(
                        b -> assertThat(b.getType()).isEqualTo("string")
                    )
            );
    }

    @io.swagger.v3.oas.annotations.media.Schema(type = "string")
    record MyString(String value) {}

    static final class MyStringDeserializer extends StdDeserializer<MyString> {

        private MyStringDeserializer(Class<MyString> t) {
            super(t);
        }

        @Override
        public MyString deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
            return new MyString(jsonParser.getText());
        }

    }

    static final class MyStringSerializer extends StdSerializer<MyString> {

        private MyStringSerializer(Class<MyString> t) {
            super(t);
        }

        @Override
        public void serialize(MyString value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(value.value());
        }

    }

}
