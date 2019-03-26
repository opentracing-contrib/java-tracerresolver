/*
 * Copyright 2017-2019 The OpenTracing Authors
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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import java.util.ArrayList;
import java.util.List;

public final class Mocks {
    static final List<Class<?>> calledConverterTypes = new ArrayList<Class<?>>();

    public static class FallbackTracer extends UnimplementedTracer {
    }

    public static class ResolvedTracer extends UnimplementedTracer {
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

    public static class ThrowingResolver extends TracerResolver {
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

    public static class ThrowingConverter implements TracerConverter {
        @Override
        public Tracer convert(Tracer existingTracer) {
            calledConverterTypes.add(getClass());
            throw new UnsupportedOperationException("Conversion of " + existingTracer + " not supported.");
        }
    }

    public static class ConvertToNull implements TracerConverter {
        @Override
        public Tracer convert(Tracer existingTracer) {
            calledConverterTypes.add(getClass());
            return null;
        }
    }

    /**
     * Unimplemented tracer because we don't actually use these mock tracers.
     * Their purpose is merely verifying what is being resolved.
     */
    private static abstract class UnimplementedTracer implements Tracer {
        @Override
        public ScopeManager scopeManager() {
            return null;
        }

        @Override
        public Span activeSpan() {
            return null;
        }

        @Override
        public Scope activateSpan(Span span) {
            return null;
        }

        @Override
        public SpanBuilder buildSpan(String operationName) {
            return null;
        }

        @Override
        public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        }

        @Override
        public <C> SpanContext extract(Format<C> format, C carrier) {
            return null;
        }

        public void close() {
        }
    }
}
