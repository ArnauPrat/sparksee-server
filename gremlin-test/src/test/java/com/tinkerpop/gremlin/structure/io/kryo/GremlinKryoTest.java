package com.tinkerpop.gremlin.structure.io.kryo;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinKryoTest {
    @Test
    public void shouldGetMostRecentVersion() {
        final GremlinKryo.Builder b = GremlinKryo.build();
        assertNotSame(b, GremlinKryo.build());
    }
}
