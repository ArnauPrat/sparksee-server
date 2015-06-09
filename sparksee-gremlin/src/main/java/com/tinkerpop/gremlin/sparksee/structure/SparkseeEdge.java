package com.tinkerpop.gremlin.sparksee.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sparsity.sparksee.gdb.EdgeData;
import com.tinkerpop.gremlin.sparksee.process.graph.SparkseeElementTraversal;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class SparkseeEdge extends SparkseeElement implements Edge, Edge.Iterators, SparkseeElementTraversal<Edge>  {

    protected static final int SCOPE = com.sparsity.sparksee.gdb.Type.EdgesType;

    protected SparkseeEdge(final Long id, final String label, final SparkseeGraph graph) {
        super(id, label, graph);
    }
    
    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }

    @Override
    public com.tinkerpop.gremlin.structure.Edge.Iterators iterators() {
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<? extends Property<?>> propertyIterator(String... keys) {
        return super.propertyIterator(keys);
    }
    
    @Override
    public Iterator<Vertex> vertexIterator(final Direction direction) {
        this.graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        EdgeData edata = rawGraph.getEdgeData(id);
        final List<Vertex> vertices = new ArrayList<Vertex>();
        String tailLabel;
        String headLabel;
        switch (direction) {
        case BOTH:
            tailLabel = rawGraph.getType(rawGraph.getObjectType(edata.getTail())).getName();
            headLabel = rawGraph.getType(rawGraph.getObjectType(edata.getHead())).getName();
            vertices.add(new SparkseeVertex(edata.getTail(), tailLabel, graph));
            vertices.add(new SparkseeVertex(edata.getHead(), headLabel, graph));
            break;
        case IN:
            headLabel = rawGraph.getType(rawGraph.getObjectType(edata.getHead())).getName();
            vertices.add(new SparkseeVertex(edata.getHead(), headLabel, graph));
            break;
        case OUT:
            tailLabel = rawGraph.getType(rawGraph.getObjectType(edata.getTail())).getName();
            vertices.add(new SparkseeVertex(edata.getTail(), tailLabel, graph));
            break;
        default:
            break;
        }
        return vertices.iterator();
    }
}
