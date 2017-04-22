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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code TracerResolver} API definition looks for one or more registered {@link TracerResolver} implementations
 * using the {@link ServiceLoader}.
 * <p>
 * If no {@link TracerResolver} implementations are found, the {@link #resolveTracer()} method will fallback to
 * {@link ServiceLoader} lookup of the {@link Tracer} service itself.
 *
 * @author Sjoerd Talsma
 */
public abstract class TracerResolver {
    private static final Logger LOGGER = Logger.getLogger(TracerResolver.class.getName());
    private static final ServiceLoader<TracerResolver> RESOLVERS = ServiceLoader.load(TracerResolver.class);
    private static final ServiceLoader<Tracer> FALLBACK = ServiceLoader.load(Tracer.class);

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
        for (TracerResolver resolver : RESOLVERS) {
            try {
                Tracer tracer = resolver.resolve();
                if (tracer != null) {
                    LOGGER.log(Level.FINER, "Resolved tracer: {0}.", tracer);
                    return tracer;
                }
            } catch (RuntimeException rte) {
                LOGGER.log(Level.WARNING, "Error resolving tracer using " + resolver + ": " + rte.getMessage(), rte);
            }
        }
        for (Tracer tracer : FALLBACK) {
            if (tracer != null) {
                LOGGER.log(Level.FINER, "Resolved tracer: {0}.", tracer);
                return tracer;
            }
        }
        LOGGER.log(Level.FINEST, "No tracer was resolved.");
        return null;
    }

    /**
     * Reloads the lazily found {@linkplain TracerResolver resolvers} and the fallback resolver.
     */
    public static void reload() {
        RESOLVERS.reload();
        FALLBACK.reload();
        LOGGER.log(Level.FINER, "Resolvers were reloaded.");
    }

}
