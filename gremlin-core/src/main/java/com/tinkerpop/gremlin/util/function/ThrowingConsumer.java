package com.tinkerpop.gremlin.util.function;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@FunctionalInterface
public interface ThrowingConsumer<A> {
    public void accept(final A a) throws Exception;
}
