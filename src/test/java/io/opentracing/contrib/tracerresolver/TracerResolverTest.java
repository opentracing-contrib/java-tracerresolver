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
import io.opentracing.mock.MockTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class TracerResolverTest {

    private static final File TRACERRESOLVER_SERVICE_FILE =
            new File("target/test-classes/META-INF/services/" + TracerResolver.class.getName());

    @Before
    public void prepareClasspath() throws IOException {
        ServiceFileUtil.writeServiceFile(TRACERRESOLVER_SERVICE_FILE, TestTracerResolver.class.getName());
    }

    @After
    public void restoreClasspath() throws IOException {
        TRACERRESOLVER_SERVICE_FILE.delete();
    }

    @Before
    @After
    public void resetTracerResolver() {
        TracerResolver.reload();
    }

    @Test
    public void testResolveTracer() {
        assertThat(TracerResolver.resolveTracer(), is(instanceOf(TestTracerResolver.TestTracer.class)));
    }

    public static class TestTracerResolver extends TracerResolver {
        private static class TestTracer extends MockTracer {
        }

        @Override
        protected Tracer resolve() {
            return new TestTracer();
        }
    }

}
