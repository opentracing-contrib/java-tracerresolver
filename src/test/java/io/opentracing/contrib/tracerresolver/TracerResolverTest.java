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

import io.opentracing.Tracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TracerResolverTest {
    private static final File TRACERRESOLVER_SERVICE_FILE =
            new File("target/test-classes/META-INF/services/" + TracerResolver.class.getName());
    private static final File TRACER_SERVICE_FILE =
            new File("target/test-classes/META-INF/services/" + Tracer.class.getName());

    @After
    public void cleanServiceFiles() throws IOException {
        if (TRACER_SERVICE_FILE.isFile()) TRACER_SERVICE_FILE.delete();
        if (TRACERRESOLVER_SERVICE_FILE.isFile()) TRACERRESOLVER_SERVICE_FILE.delete();
    }

    @Before
    @After
    public void resetTracerResolver() {
        TracerResolver.reload();
    }

    @Test
    public void testResolveFallback() throws IOException {
        writeServiceFile(TRACER_SERVICE_FILE, Mocks.FallbackTracer.class.getName());
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.FallbackTracer.class)));
    }

    @Test
    public void testResolveTracer() throws IOException {
        writeServiceFile(TRACERRESOLVER_SERVICE_FILE, Mocks.MockTracerResolver.class.getName());
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.ResolvedTracer.class)));
    }

    @Test
    public void testNothingRegistered() throws IOException {
        assertThat(TracerResolver.resolveTracer(), is(nullValue()));
    }

    @Test
    public void testFallbackWhenResolvingNull() throws IOException {
        writeServiceFile(TRACER_SERVICE_FILE, Mocks.FallbackTracer.class.getName());
        writeServiceFile(TRACERRESOLVER_SERVICE_FILE, Mocks.NullTracerResolver.class.getName());
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(Mocks.FallbackTracer.class)));
    }

    @Test
    public void testResolvingNullWithoutFallback() throws IOException {
        writeServiceFile(TRACERRESOLVER_SERVICE_FILE, Mocks.NullTracerResolver.class.getName());
        assertThat(TracerResolver.resolveTracer(), is(nullValue()));
    }

    private static void writeServiceFile(File serviceFile, String content) throws IOException {
        serviceFile.getParentFile().mkdirs();
        if (serviceFile.isFile()) serviceFile.delete();
        PrintWriter writer = new PrintWriter(new FileWriter(serviceFile));
        try {
            writer.println(content);
        } finally {
            writer.close();
        }
    }

}
