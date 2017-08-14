[![Build Status][ci-img]][ci]
[![Released Version][maven-img]][maven]

# java-tracerresolver
Resolver API for OpenTracing Tracer implementations.

## TracerResolver

This is both an abstract Service definition declaring a `resolve()` method to be implemented
and a utility class providing a static `resolveTracer()` method using the [JDK ServiceLoader][serviceloader]
to find declared `TracerResolver` implementations to resolve a Tracer.

## Fallback lookup

If no `TracerResolver` is found or no `Tracer` is returned, a [ServiceLoader lookup][serviceloader] for a declared 
`Tracer` class is used as _fallback_.

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
    by assigning a _default priority_ of `Integer#MAX_VALUE`.
 3. Finally, negative priority is applied in reverse-natural order (e.g. `-1`, `-2`, `-3`, ...).

The order of objects with equal (implicit) priority is undefined.

## GlobalTracer

If the [opentracing-util] library is detected and a [`GlobalTracer`][globaltracer] 
is already-registered, the resolving mechanism will be disabled.
In this case the [GlobalTracer] is always returned as-is, _without_ applying any converters.


  [ci-img]: https://img.shields.io/travis/opentracing-contrib/java-tracerresolver/master.svg
  [ci]: https://travis-ci.org/opentracing-contrib/java-tracerresolver
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-tracerresolver.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-tracerresolver
  [serviceloader]: http://download.java.net/java/jdk9/docs/api/java/util/ServiceLoader.html
  [priority]: http://docs.oracle.com/javaee/7/api/javax/annotation/Priority.html
  [opentracing-util]: https://github.com/opentracing/opentracing-java/tree/master/opentracing-util
  [globaltracer]: https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java
