/*
 *
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
 *   Maximillian Arruda
 *
 */
package org.eclipse.jnosql.communication.column;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnParamsTest {

    @Test
    public void shouldSetParameter() {
        Params params = Params.newParams();
        Value name = params.add("name");
        Column column = Column.of("name", name);
        params.bind("name", "Ada Lovelace");

        assertEquals("Ada Lovelace", column.get());

        params.bind("name", "Diana");
        assertEquals("Diana", column.get());
    }

}