package com.tinkerpop.gremlin.sparksee.process.graph.step.sideEffect;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.TraverserGenerator;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.GraphStep;
import com.tinkerpop.gremlin.process.util.TraversalMetrics;
import com.tinkerpop.gremlin.sparksee.structure.SparkseeGraph;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;

public class SparkseeGraphStep<E extends Element> extends GraphStep<E> {

    private final SparkseeGraph graph;
    
    @SuppressWarnings("rawtypes")
    public SparkseeGraphStep(final Traversal traversal, final Class<E> returnClass, final SparkseeGraph graph) {
        super(traversal, returnClass);
        this.graph = graph;
    }
    
    @Override
    public void generateTraversers(final TraverserGenerator traverserGenerator) {
        if (PROFILING_ENABLED) TraversalMetrics.start(this);
        this.start = Vertex.class.isAssignableFrom(this.returnClass) ? graph.vertices() : graph.edges();
        super.generateTraversers(traverserGenerator);
        if (PROFILING_ENABLED) TraversalMetrics.stop(this);
    }
}
