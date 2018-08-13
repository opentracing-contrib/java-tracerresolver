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

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.opentracing.contrib.tracerresolver.PriorityComparator.prioritize;

/**
 * {@code TracerResolver} API definition looks for one or more registered {@link TracerResolver} implementations
 * using the {@link ServiceLoader}.
 * <p>
 * If no {@link TracerResolver} implementations are found, the {@link #resolveTracer()} method will fallback to
 * {@link ServiceLoader} lookup of the {@link Tracer} service itself.
 * <p>
 * Available {@link TracerConverter} implementations are applied to the resolved {@link Tracer} instance.
 * <p>
 * None of this happens if there is an existing {@code GlobalTracer} explicit registration.
 * That will always be returned (as-is) by the resolver, if available.
 *
 * @author Sjoerd Talsma
 */
public abstract class TracerResolver {
    private static final Logger LOGGER = Logger.getLogger(TracerResolver.class.getName());

    /**
     * Resolves the {@link Tracer} implementation.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    protected abstract Tracer resolve();

    /**
     * Detects all {@link TracerResolver} service implementations and attempts to resolve a {@link Tracer}.
     * <p>
     * If a {@code GlobalTracer} has been previously registered, it will be returned before attempting to resolve
     * a {@linkplain Tracer} on our own.
     * <p>
     * If there are more than one resolver, the first non-<code>null</code> resolved tracer is returned.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    public static Tracer resolveTracer() {
        try { // Take care NOT to import GlobalTracer as it is an optional dependency and may not be on the classpath.
            if (io.opentracing.util.GlobalTracer.isRegistered()) {
                return logResolved(io.opentracing.util.GlobalTracer.get());
            }
        } catch (NoClassDefFoundError globalTracerNotInClasspath) {
            LOGGER.finest("GlobalTracer is not found on the classpath.");
        }

        if (!TracerResolver.isDisabled()) {
            for (TracerResolver resolver : prioritize(ServiceLoader.load(TracerResolver.class))) {
                try {
                    Tracer tracer = convert(resolver.resolve());
                    if (tracer != null) {
                        return logResolved(tracer);
                    }
                } catch (RuntimeException rte) {
                    LOGGER.log(Level.WARNING, "Error resolving tracer using " + resolver + ": " + rte.getMessage(), rte);
                }
            }
            for (Tracer tracer : prioritize(ServiceLoader.load(Tracer.class))) {
                tracer = convert(tracer);
                if (tracer != null) {
                    return logResolved(tracer);
                }
            }
        }
        LOGGER.log(Level.FINEST, "No tracer was resolved.");
        return null;
    }

    /**
     * Reloads the lazily found {@linkplain TracerResolver resolvers} and the fallback resolver.
     *
     * @deprecated This method is now no-op. It's safe to just remove this method call, as there's no caching anymore.
     */
    @Deprecated
    public static void reload() {
        LOGGER.log(Level.FINER, "No-op for this implementation.");
    }

    /**
     * There are two ways to globally disable the tracer resolver:
     * <ul>
     * <li>Setting a {@code "tracerresolver.disabled"} system property to {@code true}</li>
     * <li>Setting the environment variable {@code TRACERRESOLVER_DISABLED} to {@code true}</li>
     * </ul>
     *
     * @return Whether the tracer resolver mechanism is disabled ({@code false} by default).
     */
    private static boolean isDisabled() {
        String prop = System.getProperty("tracerresolver.disabled", System.getenv("TRACERRESOLVER_DISABLED"));
        return prop != null && (prop.equals("1") || prop.equalsIgnoreCase("true"));
    }

    private static Tracer convert(Tracer resolved) {
        if (resolved != null) {
            for (TracerConverter converter : prioritize(ServiceLoader.load(TracerConverter.class))) {
                try {
                    Tracer converted = converter.convert(resolved);
                    LOGGER.log(Level.FINEST, "Converted {0} using {1}: {2}.", new Object[]{resolved, converter, converted});
                    resolved = converted;
                } catch (RuntimeException rte) {
                    LOGGER.log(Level.WARNING, "Error converting " + resolved + " with " + converter + ": " + rte.getMessage(), rte);
                }
                if (resolved == null) break;
            }
        }
        return resolved;
    }

    private static Tracer logResolved(Tracer resolvedTracer) {
        LOGGER.log(Level.FINER, "Resolved tracer: {0}.", resolvedTracer);
        return resolvedTracer;
    }

}
