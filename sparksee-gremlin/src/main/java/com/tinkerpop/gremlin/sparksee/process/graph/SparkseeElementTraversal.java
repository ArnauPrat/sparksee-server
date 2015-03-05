package com.tinkerpop.gremlin.sparksee.process.graph;

import com.tinkerpop.gremlin.process.graph.ElementTraversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.StartStep;
import com.tinkerpop.gremlin.process.graph.util.DefaultGraphTraversal;
import com.tinkerpop.gremlin.structure.Element;

public interface SparkseeElementTraversal<A extends Element> extends ElementTraversal<A> {
    public default GraphTraversal<A, A> start() {
        GraphTraversal<A, A> traversal = new DefaultGraphTraversal<A, A>();
        traversal.addStep(new StartStep<>(traversal, this));
        return traversal;
    }
}
