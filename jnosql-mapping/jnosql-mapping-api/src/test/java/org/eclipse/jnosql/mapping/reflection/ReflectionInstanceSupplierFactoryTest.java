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
package org.eclipse.jnosql.mapping.reflection;

import org.eclipse.jnosql.mapping.Convert;
import org.eclipse.jnosql.mapping.VetedConverter;
import org.eclipse.jnosql.mapping.test.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions(EntityMetadataExtension.class)
class ReflectionInstanceSupplierFactoryTest {


    @Inject
    private ReflectionInstanceSupplierFactory instanceSupplier;


    @Test
    public void shouldNewInstance() {
        Constructor<?>[] declaredConstructors = Person.class.getDeclaredConstructors();
        Constructor<?> constructor = Stream.of(declaredConstructors).peek(c -> c.setAccessible(true))
                .filter(c -> c.getParameterCount() == 0)
                .findFirst().get();

        InstanceSupplier instanceSupplier = this.instanceSupplier.apply(constructor);

        Person person = (Person) instanceSupplier.get();
        assertEquals(Person.class, person.getClass());

    }

    @Test
    public void shouldCreateInstanceSupplier() {
        Constructor<?>[] declaredConstructors = Person.class.getDeclaredConstructors();
        Constructor<?> constructor = Stream.of(declaredConstructors).filter(c -> c.getParameterCount() == 0)
                .findFirst().get();

        InstanceSupplier instanceSupplier = this.instanceSupplier.apply(constructor);
        Assertions.assertNotNull(instanceSupplier);
    }
}