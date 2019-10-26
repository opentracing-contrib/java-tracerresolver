[![Build Status][ci-img]][ci]
[![Released Version][maven-img]][maven]

# java-tracerresolver
Resolver API for OpenTracing Tracer implementations.

**NOTE:** The Tracer Resolver mechanism is only intended to be used at application startup/initialization time.
This responsibility should be handled by the application, possibly using some runtime specific support (e.g. providing a
Tracer `@Bean` in Spring Boot, or a CDI producer).
Framework integrations used to instrument specific technologies should **not** use this library, but should allow a Tracer to be injected instead, with fallback to the GlobalTracer.

## Tracer resolver

This is a utility class providing a static `resolveTracer()` method using 
the [JDK ServiceLoader][serviceloader] to find declared `TracerFactory` implementations 
providing a Tracer.

## Tracer factory

A tracer factory implements a `getTracer()` method and is used by the `TracerResolver`
to look up tracer implementations via a [JDK ServiceLoader][serviceloader].

## Fallback lookup

If no `TracerFactory` is found or no `Tracer` is returned, 
a [ServiceLoader lookup][serviceloader] for a declared concrete `TracerResolver` class is used.
This supports the 'legacy' lookup for TracerResolver subclasses providing a `resolve()` implementation.
Finally, if this also fails, the `Tracer` class is used as last-resort lookup.

## Tracer converters

A resolved tracer is passed to _all_ `TracerConverter` instances that were found.

Tracer converters can be useful for _automatically wrapping_ the resolved `Tracer`:
```java
public final class FooWrapperConverter implements TracerConverter {
    public Tracer convert(Tracer existingTracer) {
        return new FooTracerWrapper(existingTracer);
    }
}
```

## Priority

If multiple `TracerResolver`, `TracerConverter` or `Tracer` implementations are found,
they are checked for presence of the [`@Priority`][priority] annotation 
on their class or superclasses. 
The priority is applied as follows:
 1. First, non-negative priority is applied in natural order (e.g. `0`, `1`, `2`, ...).
 2. Next, objects without [`@Priority`][priority] annotation are applied
    by assigning a _default priority_ of `Integer.MAX_VALUE`.
 3. Finally, negative priority is applied in reverse-natural order (e.g. `-1`, `-2`, `-3`, ...).

The order of objects with equal (implicit) priority is undefined.

## GlobalTracer

If the [opentracing-util] library is detected and a [`GlobalTracer`][globaltracer] 
is already-registered, the resolving mechanism will be disabled.
In this case the [GlobalTracer] is always returned as-is, _without_ applying any converters.


  [ci-img]: https://img.shields.io/travis/opentracing-contrib/java-tracerresolver/master.svg
  [ci]: https://travis-ci.org/opentracing-contrib/java-tracerresolver
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-tracerresolver.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22opentracing-tracerresolver%22
  [serviceloader]: https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html
  [priority]: http://docs.oracle.com/javaee/7/api/javax/annotation/Priority.html
  [opentracing-util]: https://github.com/opentracing/opentracing-java/tree/master/opentracing-util
  [globaltracer]: https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java
