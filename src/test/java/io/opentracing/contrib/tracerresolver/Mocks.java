package io.opentracing.contrib.tracerresolver;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

public final class Mocks {

    public static class FallbackTracer extends MockTracer {
    }

    public static class ResolvedTracer extends MockTracer {
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

}
