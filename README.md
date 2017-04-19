[![Build Status][ci-img]][ci]
[![Released Version][maven-img]][maven]

# java-tracerresolver
Resolver API for OpenTracing Tracer implementations.

## TracerResolver

This is both an abstract Service definition declaring an abstract `resolve()` method and a
static lookup utility that provides a static `resolveTracer()` method 
that uses the [JDK ServiceLoader][serviceloader]
to find declared `TracerResolver` implementations to resolve a Tracer.

## Fallback resolver

If no resolver is found, a [ServiceLoader lookup][serviceloader] for a declared 
`Tracer` implementation is used as fallback resolver.

  [ci-img]: https://img.shields.io/travis/opentracing-contrib/java-tracerresolver/master.svg
  [ci]: https://travis-ci.org/opentracing-contrib/java-tracerresolver
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-tracerresolver.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-tracerresolver
  [serviceloader]: http://download.java.net/java/jdk9/docs/api/java/util/ServiceLoader.html
