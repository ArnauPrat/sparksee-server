package com.tinkerpop.gremlin.sparksee.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sparsity.sparksee.gdb.EdgesDirection;
import com.tinkerpop.gremlin.sparksee.process.graph.SparkseeElementTraversal;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class SparkseeVertex extends SparkseeElement implements Vertex, Vertex.Iterators, SparkseeElementTraversal<Vertex> {

    protected static final int SCOPE = com.sparsity.sparksee.gdb.Type.NodesType;
    
    protected static final EdgesDirection SPARKSEE_IN   = EdgesDirection.Ingoing;
    protected static final EdgesDirection SPARKSEE_OUT  = EdgesDirection.Outgoing;
    protected static final EdgesDirection SPARKSEE_BOTH = EdgesDirection.Any;
    
    protected static Map<Direction, EdgesDirection> directionMapper = new HashMap<Direction, EdgesDirection>();
    static {
        directionMapper.put(Direction.IN,   SPARKSEE_IN);
        directionMapper.put(Direction.OUT,  SPARKSEE_OUT);
        directionMapper.put(Direction.BOTH, SPARKSEE_BOTH);
    }
    
    protected SparkseeVertex(final Long id, final String label, final SparkseeGraph graph) {
        super(id, label, graph);
    }
    
    protected Long getId() {
        return (Long) id;
    }
    
    @Override
    public Edge addEdge(final String label, final Vertex inVertex, final Object... keyValues) {
        ElementHelper.validateLabel(label);
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        if (ElementHelper.getIdValue(keyValues).isPresent()) {
            throw Edge.Exceptions.userSuppliedIdsNotSupported();
        }

        ((SparkseeTransaction)this.graph.tx()).write();
        graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        int type = rawGraph.findType(label);
        if (type == SparkseeGraph.INVALID_TYPE) {
            type = rawGraph.newEdgeType(label, true, true);
        }
        assert type != SparkseeGraph.INVALID_TYPE;
        assert inVertex instanceof SparkseeVertex;
        
        long oid = rawGraph.newEdge(type, (Long) id, ((SparkseeVertex) inVertex).getId());
        Edge edge = new SparkseeEdge(oid, label, graph);
        ElementHelper.attachProperties(edge, keyValues);
        return edge;
    }

    @Override
    @SuppressWarnings("unchecked")
    public VertexProperty<?> property(final String key) {
        Property<?> property = super.property(key);
        if (property == Property.empty()) { 
            return VertexProperty.empty();
        } else {
            return new SparkseeVertexProperty<>(this, super.property(key));
        }
    }
    
    @Override
    public <V> VertexProperty<V> property(final String key, final V value) {
        return new SparkseeVertexProperty<V>(this, super.property(key, value));
    }
    
    @Override
    public com.tinkerpop.gremlin.structure.Vertex.Iterators iterators() {
        return this;
    }
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Iterator<Edge> edgeIterator(Direction direction, String... labels) {
        graph.tx().readWrite();
        return new SparkseeHelper.SparkseeEdgeIterator(graph, this, 
                directionMapper.get(direction), SparkseeHelper.NO_LIMT, labels);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Iterator<Vertex> vertexIterator(Direction direction, String... labels) {
        graph.tx().readWrite();
        return new SparkseeHelper.SparkseeVertexIterator(graph, this, 
                directionMapper.get(direction), SparkseeHelper.NO_LIMT, labels);
    }

    @Override
    public Iterator<VertexProperty<?>> hiddenPropertyIterator(String... keys) {
        Iterator<? extends Property<?>> original = super.hiddenPropertyIterator(keys);
        ArrayList<VertexProperty<?>> copy = new ArrayList<VertexProperty<?>>();
        while (original.hasNext()) {
            Property<?> prop = original.next();
            copy.add(new SparkseeVertexProperty<>(this, prop));
        }
        return copy.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<VertexProperty<?>> propertyIterator(String... keys) {
        Iterator<? extends Property<?>> original = super.propertyIterator(keys);
        ArrayList<VertexProperty<?>> copy = new ArrayList<VertexProperty<?>>();
        while (original.hasNext()) {
            Property<?> prop = original.next();
            copy.add(new SparkseeVertexProperty<>(this, prop));
        }
        return copy.iterator();
    }
    
    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }
}
