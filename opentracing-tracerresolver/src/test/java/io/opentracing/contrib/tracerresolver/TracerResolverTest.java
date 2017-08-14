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

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TracerResolverTest {
    private static final File SERVICES_DIR = new File("target/test-classes/META-INF/services/");

    @After
    public void cleanServiceFiles() throws IOException {
        new File(SERVICES_DIR, TracerResolver.class.getName()).delete();
        new File(SERVICES_DIR, Tracer.class.getName()).delete();
    }

    @Before
    @After
    public void resetTracerResolver() {
        TracerResolver.reload();
    }

    @Before
    @After
    public void clearGlobalTracer() throws NoSuchFieldException, IllegalAccessException {
        Field globalTracer = GlobalTracer.class.getDeclaredField("tracer");
        globalTracer.setAccessible(true);
        globalTracer.set(null, NoopTracerFactory.create());
    }

    @Test
    public void testNothingRegistered() throws IOException {
        assertThat(TracerResolver.resolveTracer(), is(nullValue()));
    }

    @Test
    public void testResolveTracer() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class);
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.ResolvedTracer.class)));
    }

    @Test
    public void testResolveFallback() throws IOException {
        writeServiceFile(Tracer.class, Mocks.FallbackTracer.class);
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.FallbackTracer.class)));
    }

    @Test
    public void testResolverBeatsFallback() throws IOException {
        writeServiceFile(Tracer.class, Mocks.FallbackTracer.class);
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class);
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.ResolvedTracer.class)));
    }

    @Test
    public void testFallbackWhenResolvingNull() throws IOException {
        writeServiceFile(Tracer.class, Mocks.FallbackTracer.class);
        writeServiceFile(TracerResolver.class, Mocks.NullTracerResolver.class);
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.FallbackTracer.class)));
    }

    @Test
    public void testResolvingNullWithoutFallback() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.NullTracerResolver.class);
        assertThat(TracerResolver.resolveTracer(), is(nullValue()));
    }

    @Test
    public void testSkipResolverThrowingException() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.HighPriorityThrowingResolver.class, Mocks.MockTracerResolver.class);
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.ResolvedTracer.class)));
    }

    @Test
    public void testResolveWithExistingGlobalTracer() throws IOException {
        writeServiceFile(TracerResolver.class, Mocks.MockTracerResolver.class, Mocks.NullTracerResolver.class);
        writeServiceFile(Tracer.class, Mocks.FallbackTracer.class);
        GlobalTracer.register(new MockTracer());
        assertThat(TracerResolver.resolveTracer(), is(sameInstance(GlobalTracer.get())));
    }

    static <SVC> void writeServiceFile(Class<SVC> service, Class<?>... implementations) throws IOException {
        SERVICES_DIR.mkdirs();
        File serviceFile = new File(SERVICES_DIR, service.getName());
        if (serviceFile.isFile()) serviceFile.delete();
        PrintWriter writer = new PrintWriter(new FileWriter(serviceFile));
        try {
            for (Class<?> implementation : implementations) {
                writer.println(implementation.getName());
            }
        } finally {
            writer.close();
        }
    }

}
