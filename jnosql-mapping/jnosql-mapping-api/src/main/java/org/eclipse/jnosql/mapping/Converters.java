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
package org.eclipse.jnosql.mapping;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * The {@link Convert} collection, this instance will generate/create an instance.
 */
@ApplicationScoped
public class Converters {

    private static final Logger LOGGER = Logger.getLogger(Converters.class.getName());

    @Inject
    private BeanManager beanManager;

    @Inject
    private InstanceProducer instanceProducer;

    /**
     * Returns a converter instance where it might use scope from CDI.
     *
     * @param converterClass the converter class
     * @param <X> the type of the entity attribute
     * @param <Y> the type of the database column
     * @return a converter instance
     * @throws NullPointerException when converter is null
     */
    public <X, Y> AttributeConverter<X, Y> get(Class<? extends AttributeConverter<X, Y>> converterClass) {
        Objects.requireNonNull(converterClass, "The converterClass is required");
        return getInstance(converterClass);
    }

    private <T> T getInstance(Class<T> entity) {
        Iterator<Bean<?>> iterator = beanManager.getBeans(entity).iterator();
        if (iterator.hasNext()) {
            Bean<T> bean = (Bean<T>) iterator.next();
            CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(bean, entity, ctx);
        } else {
            LOGGER.info("The entity type: " + entity + " not found on CDI context, creating by constructor");
            return instanceProducer.create(entity);
        }

    }

    @Override
    public String toString() {
        return "DefaultConverters{" +
                "beanManager=" + beanManager +
                '}';
    }
}
