/*
 * Copyright 2017-2018 The OpenTracing Authors
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

import javax.annotation.Priority;
import java.util.ArrayList;
import java.util.List;

public final class Mocks {
    static final List<Class<?>> calledConverterTypes = new ArrayList<Class<?>>();

    public static class FallbackTracer extends MockTracer {
    }

    public static class ResolvedTracer extends MockTracer {
    }

    public static class ResolvedTracerFromFactory extends MockTracer {
    }

    public static class MockTracerResolver extends TracerResolver {
        @Override
        protected Tracer resolve() {
            return new ResolvedTracer();
        }
    }

    public static class NullTracerResolver extends TracerResolver {
        @Override
        protected Tracer resolve() {
            return null;
        }
    }

    @Priority(1)
    public static class HighPriorityThrowingResolver extends TracerResolver {
        @Override
        protected Tracer resolve() {
            throw new IllegalStateException("Can't resolve tracer (missing configuration?)");
        }
    }

    public static class IdentityConverter implements TracerConverter {
        @Override
        public Tracer convert(Tracer existingTracer) {
            calledConverterTypes.add(getClass());
            return existingTracer;
        }
    }

    @Priority(5)
    public static class Prio5_ThrowingConverter implements TracerConverter {
        @Override
        public Tracer convert(Tracer existingTracer) {
            calledConverterTypes.add(getClass());
            throw new UnsupportedOperationException("Conversion of " + existingTracer + " not supported.");
        }
    }

    @Priority(10)
    public static class Prio10_ConvertToNull implements TracerConverter {
        @Override
        public Tracer convert(Tracer existingTracer) {
            calledConverterTypes.add(getClass());
            return null;
        }
    }

    @Priority(0)
    public static class Prio0_TracerFactory implements TracerFactory {
        @Override
        public Tracer getTracer() {
            return new ResolvedTracerFromFactory();
        }
    }

    @Priority(1)
    public static class Prio1_TracerResolver extends TracerResolver {
        @Override
        protected Tracer resolve() {
            return new ResolvedTracer();
        }
    }
}
