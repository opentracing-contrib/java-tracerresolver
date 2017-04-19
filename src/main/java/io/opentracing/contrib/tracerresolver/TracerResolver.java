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

import java.util.ServiceLoader;

/**
 * {@code TracerResolver} API definition looks for one or more registered {@link TracerResolver} implementations
 * using the {@link ServiceLoader}.
 * <p>
 * If no {@link TracerResolver} implementations are found, the {@link #resolveTracer()} method will fallback to
 * {@link ServiceLoader} lookup of the {@link Tracer} service itself.
 */
public abstract class TracerResolver {
    private static final ServiceLoader<TracerResolver> RESOLVERS = ServiceLoader.load(TracerResolver.class);
    private static final ServiceLoader<Tracer> FALLBACK = ServiceLoader.load(Tracer.class);

    private static Tracer resolved = null;

    /**
     * Resolves the {@link Tracer} implementation.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    protected abstract Tracer resolve();

    /**
     * Detects all {@link TracerResolver} service implementations and attempts to resolve a {@link Tracer}.
     * <p>
     * If there are more than one resolver, the first non-<code>null</code> resolved tracer is returned.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    public static Tracer resolveTracer() {
        if (resolved == null) {
            // TODO error handling?
            for (TracerResolver resolver : RESOLVERS) {
                Tracer tracer = resolver.resolve();
                if (tracer != null) {
                    resolved = tracer;
                    return resolved;
                }
            }
            for (Tracer tracer : FALLBACK) {
                if (tracer != null) {
                    resolved = tracer;
                    break;
                }
            }
        }
        return resolved;
    }

    public static void reset() {
        RESOLVERS.reload();
        FALLBACK.reload();
        resolved = null;
    }

}
