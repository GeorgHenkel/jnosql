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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.validation.Validator;
import jakarta.validation.Validation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolationException;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * This implementation defines the workflow to insert an Entity on {@link jakarta.nosql.keyvalue.KeyValueTemplate}.
 * The default implementation follows:
 * <p>{@link KeyValueEventPersistManager#firePreEntity(Object)}</p>
 * <p>{@link KeyValueEntityConverter#toKeyValue(Object)}</p>
 * <p>Database alteration</p>
 * <p>{@link KeyValueEventPersistManager#firePostEntity(Object)}</p>
 */
public abstract class KeyValueWorkflow {

    protected abstract KeyValueEventPersistManager getEventManager();


    protected abstract KeyValueEntityConverter getConverter();


    /**
     * Executes the workflow to do an interaction on a database key-value.
     *
     * @param entity the entity to be saved
     * @param action the alteration to be executed on database
     * @param <T>    the entity type
     * @return after the workflow the entity response
     * @see jakarta.nosql.keyvalue.KeyValueTemplate#put(Object, java.time.Duration)  {@link jakarta.nosql.keyvalue.KeyValueTemplate#put(Object)}
     * DocumentTemplate#update(Object)
     */
    public <T> T flow(T entity, UnaryOperator<KeyValueEntity> action) {

        Function<T, T> flow = getFlow(entity, action);

        return flow.apply(entity);

    }

    private <T> Function<T, T> getFlow(T entity, UnaryOperator<KeyValueEntity> action) {
        UnaryOperator<T> validation = this::validate;

        UnaryOperator<T> firePreEntity = t -> {
            getEventManager().firePreEntity(t);
            return t;
        };


        Function<T, KeyValueEntity> convertKeyValue = t -> getConverter().toKeyValue(t);

        Function<KeyValueEntity, T> converterEntity = t -> getConverter().toEntity((Class<T>) entity.getClass(), t);

        UnaryOperator<T> firePostEntity = t -> {
            getEventManager().firePostEntity(t);
            return t;
        };

        return validation
                .andThen(firePreEntity)
                .andThen(convertKeyValue)
                .andThen(action)
                .andThen(converterEntity)
                .andThen(firePostEntity);
    }

    private <T> T validate(T entity) {
        Objects.requireNonNull(entity, "entity is required");

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            final var constraintViolations = validator.validate(entity);
            if (!constraintViolations.isEmpty()) {
                var violations = constraintViolations
                        .stream()
                        .map(ConstraintViolation::toString)
                        .collect(Collectors.joining("\n"));

                var violationMessage = "Validation failed for %s \nList of constraint violations: [\n%s]"
                        .formatted(entity.getClass().getName(), violations);

                throw new ConstraintViolationException(violationMessage, constraintViolations);
            }
        }

        return entity;
    }
}
