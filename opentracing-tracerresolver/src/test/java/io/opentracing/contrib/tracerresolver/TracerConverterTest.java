/*
 * Copyright 2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.tracerresolver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static io.opentracing.contrib.tracerresolver.TracerResolverTest.writeServiceFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TracerConverterTest {
    private static final File SERVICES_DIR = new File("target/test-classes/META-INF/services/");

    @After
    public void cleanServiceFiles() throws IOException {
        new File(SERVICES_DIR, TracerResolver.class.getName()).delete();
        new File(SERVICES_DIR, TracerConverter.class.getName()).delete();
    }

    @Before
    @After
    public void resetTracerResolver() {
        Mocks.calledConverterTypes.clear();
        TracerResolver.reload();
    }

    @Test
    public void testConverterNotCalledWhenNothingRegistered() throws IOException {
        writeServiceFile(TracerConverter.class, Mocks.IdentityConverter.class);
        assertThat("No tracer should be resolved", TracerResolver.resolveTracer(), is(nullValue()));
        assertThat("No converter should be called", Mocks.calledConverterTypes, hasSize(0));
    }

    @Test
    public void testTrivialConverterExample() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class);
        writeServiceFile(TracerConverter.class, Mocks.IdentityConverter.class);

        assertThat("Tracer must be resolved", TracerResolver.resolveTracer(), instanceOf(Mocks.ResolvedTracer.class));
        assertThat("Converter must be called", Mocks.calledConverterTypes, contains((Class) Mocks.IdentityConverter.class));
    }

    @Test
    public void testConverterNotCalledWhenProviderReturnsNull() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.NullTracerResolver.class);
        writeServiceFile(TracerConverter.class, Mocks.IdentityConverter.class);

        assertThat("Resolved tracer", TracerResolver.resolveTracer(), is(nullValue()));
        assertThat("No converter should be called", Mocks.calledConverterTypes, hasSize(0));
    }

    @Test
    public void testConverterThrowingException() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class);
        // although identity converter is listed first, the @Priority(5) should let throwing converter be called first!
        writeServiceFile(TracerConverter.class, Mocks.IdentityConverter.class, Mocks.Prio5_ThrowingConverter.class);

        assertThat("Resolved tracer", TracerResolver.resolveTracer(), instanceOf(Mocks.ResolvedTracer.class));
        assertThat("Continue after exception", Mocks.calledConverterTypes, hasSize(2));
        assertThat("Predictable order", Mocks.calledConverterTypes,
                contains((Class) Mocks.Prio5_ThrowingConverter.class, Mocks.IdentityConverter.class));
    }

    @Test
    public void testconvertToNull() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class);
        // although identity converter is listed first, the @Priority(10) should let convertToNull be called first!
        writeServiceFile(TracerConverter.class, Mocks.IdentityConverter.class, Mocks.Prio10_ConvertToNull.class);

        assertThat("Tracer after conversion(s)", TracerResolver.resolveTracer(), is(nullValue()));
        assertThat("Second converter shouldn't be called with <null>", Mocks.calledConverterTypes, hasSize(1));
        assertThat("Prioritized converter", Mocks.calledConverterTypes, contains((Class) Mocks.Prio10_ConvertToNull.class));
    }

}
