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
package org.eclipse.jnosql.mapping.reflection.collection;

import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class TreeSetSupplierTest {

    private CollectionSupplier<?> supplier;

    @BeforeEach
    public void setUp() {
        this.supplier = new TreeSetSupplier();
    }

    @Test
    public void shouldTest() {
        Assertions.assertTrue(supplier.test(NavigableSet.class));
        Assertions.assertTrue(supplier.test(SortedSet.class));
        Assertions.assertFalse(supplier.test(Set.class));
    }

    @Test
    public void shouldGet() {
        final Collection<?> collection = this.supplier.get();
        Assertions.assertNotNull(collection);
        Assertions.assertTrue(collection instanceof TreeSet);
    }
}