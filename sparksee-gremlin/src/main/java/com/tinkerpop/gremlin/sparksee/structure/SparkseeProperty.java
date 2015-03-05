package com.tinkerpop.gremlin.sparksee.structure;

import com.sparsity.sparksee.gdb.Value;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class SparkseeProperty<V> implements Property<V> {

    private final Graph graph;
    private final Element element;
    private final String key;
    private final Integer id;
    private final V value;

    public SparkseeProperty(final Graph graph, final Element element, final String key, final Integer id, final V value) {
        this.element = element;
        this.key = key;
        this.id = id;
        this.value = value;
        this.graph = graph;
    }

    @Override
    public Element element() {
        return element;
    }
    
    @Override
    public String key() {
        return key;
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public boolean isPresent() {
        return value != null;
    }

    @Override
    public boolean isHidden() {
        return Graph.Key.isHidden(key);
    }

    @Override
    public void remove() {
        try {
            ((SparkseeTransaction)this.graph.tx()).write();
            graph.tx().readWrite();
            com.sparsity.sparksee.gdb.Graph rawGraph  = ((SparkseeTransaction) graph.tx()).getRawGraph();
            Value v = new Value();
            v.setNull();
            rawGraph.setAttribute((Long) element.id(), id, v);
        } catch (Exception e) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
    }
    
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }
    
    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode() + element.hashCode();
    }
    
    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }
}
