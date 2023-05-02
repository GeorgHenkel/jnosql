/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.document;

import org.eclipse.jnosql.mapping.AttributeConverter;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.mapping.reflection.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;
import org.eclipse.jnosql.mapping.reflection.GenericFieldMapping;
import org.eclipse.jnosql.mapping.reflection.MappingType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

enum FieldConverter {
    EMBEDDED {
        @Override
        public <X, Y, T> void convert(T instance, List<Document> documents, Document document,
                                      FieldMapping field, DocumentEntityConverter converter) {

            Field nativeField = field.nativeField();
            Object subEntity = converter.toEntity(nativeField.getType(), documents);
            EntityMetadata mapping = converter.getEntities().get(subEntity.getClass());
            boolean areAllFieldsNull = mapping.fields()
                    .stream()
                    .map(f -> f.read(subEntity))
                    .allMatch(Objects::isNull);
            if (!areAllFieldsNull) {
                field.write(instance, subEntity);
            }

        }
    }, ENTITY {
        @Override
        public <X, Y, T> void convert(T instance, List<Document> documents, Document document,
                                      FieldMapping field, DocumentEntityConverter converter) {

            if (Objects.nonNull(document)) {
                converterSubDocument(instance, document, field, converter);
            } else {
                field.write(instance, converter.toEntity(field.nativeField().getType(), documents));
            }
        }

        private <T> void converterSubDocument(T instance, Document sudDocument, FieldMapping field,
                                              DocumentEntityConverter converter) {
            Object value = sudDocument.get();
            if (value instanceof Map) {
                Map map = (Map) value;
                List<Document> embeddedDocument = new ArrayList<>();

                for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                    embeddedDocument.add(Document.of(entry.getKey().toString(), entry.getValue()));
                }
                field.write(instance, converter.toEntity(field.nativeField().getType(), embeddedDocument));

            } else {
                field.write(instance, converter.toEntity(field.nativeField().getType(),
                        sudDocument.get(new TypeReference<List<Document>>() {
                        })));
            }
        }
    }, COLLECTION {
        @Override
        public <X, Y, T> void convert(T instance, List<Document> documents, Document document,
                                      FieldMapping field, DocumentEntityConverter converter) {

            if (Objects.nonNull(document)) {
                GenericFieldMapping genericField = (GenericFieldMapping) field;
                Collection collection = genericField.getCollectionInstance();
                List<List<Document>> embeddable = (List<List<Document>>) document.get();
                for (List<Document> documentList : embeddable) {
                    Object element = converter.toEntity(genericField.getElementType(), documentList);
                    collection.add(element);
                }
                field.write(instance, collection);
            }
        }
    }, DEFAULT {
        @Override
        public <X, Y, T> void convert(T instance, List<Document> documents, Document document,
                                      FieldMapping field, DocumentEntityConverter converter) {


            if (Objects.nonNull(document)) {
                Value value = document.value();
                Optional<Class<? extends AttributeConverter<X, Y>>> optionalConverter = field.getConverter();
                if (optionalConverter.isPresent()) {
                    AttributeConverter<X, Y> attributeConverter = converter.getConverters().get(optionalConverter.get());
                    Y attr = (Y)(value.isInstanceOf(List.class) ? document : value.get());
                    Object attributeConverted = attributeConverter.convertToEntityAttribute(attr);
                    field.write(instance, field.value(Value.of(attributeConverted)));
                } else {
                    field.write(instance, field.value(value));
                }
            }
        }
    };


    abstract <X, Y, T> void convert(T instance, List<Document> documents, Document document, FieldMapping field,
                                    DocumentEntityConverter converter);

    <X, Y, T> void convert(T instance, Document document, FieldMapping field,
                           DocumentEntityConverter converter) {
        convert(instance, null, document, field, converter);
    }

    static FieldConverter get(FieldMapping field) {
        if (MappingType.EMBEDDED.equals(field.type())) {
            return EMBEDDED;
        } else if (MappingType.ENTITY.equals(field.type())) {
            return ENTITY;
        } else if (isCollectionEmbeddable(field)) {
            return COLLECTION;
        } else {
            return DEFAULT;
        }
    }

    private static boolean isCollectionEmbeddable(FieldMapping field) {
        return MappingType.COLLECTION.equals(field.type()) && ((GenericFieldMapping) field).isEmbeddable();
    }
}
