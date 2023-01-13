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
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.reflection.Reflections;

import java.util.Objects;

/**
 * An instance producer from the Class
 */
@ApplicationScoped
public class InstanceProducer {

    @Inject
    private Reflections reflections;

    /**
     * Creates an instance from the class
     *
     * @param instanceType the instance class
     * @param <T>          the instance type
     * @return a new instance
     * @throws NullPointerException when instanceType is null
     */
    public <T> T create(Class<T> instanceType) {
        Objects.requireNonNull(instanceType, "instanceType is required");
        return reflections.newInstance(instanceType);
    }

}
