[![Build Status][ci-img]][ci]
[![Released Version][maven-img]][maven]

# java-tracerresolver
Resolver API for OpenTracing Tracer implementations.

## TracerResolver

This is both an abstract Service definition declaring a `resolve()` method to be implemented
and a utility class providing a static `resolveTracer()` method using the [JDK ServiceLoader][serviceloader]
to find declared `TracerResolver` implementations to resolve a Tracer.

## Fallback resolver

If no resolver is found, a [ServiceLoader lookup][serviceloader] for a declared 
`Tracer` class is used as _fallback_ resolver.

  [ci-img]: https://img.shields.io/travis/opentracing-contrib/java-tracerresolver/master.svg
  [ci]: https://travis-ci.org/opentracing-contrib/java-tracerresolver
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-tracerresolver.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-tracerresolver
  [serviceloader]: http://download.java.net/java/jdk9/docs/api/java/util/ServiceLoader.html
