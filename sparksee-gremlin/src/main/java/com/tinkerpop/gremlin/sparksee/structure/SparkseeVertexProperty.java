package com.tinkerpop.gremlin.sparksee.structure;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.structure.util.StringFactory;

public class SparkseeVertexProperty<V> implements VertexProperty<V>, VertexProperty.Iterators {

    SparkseeVertex vertex;
    Property<V> property;
    
    SparkseeVertexProperty(final SparkseeVertex vertex, final Property<V> prop) {
        this.vertex = vertex;
        this.property = prop;
    }
    
    @Override
    public String key() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return property.key();
    }

    @Override
    public V value() throws NoSuchElementException {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return property.value();
    }

    @Override
    public boolean isPresent() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return property.isPresent();
    }

    @Override
    public boolean isHidden() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return property.isHidden();
    }

    @Override
    public void remove() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        property.remove();
        property = null;
    }

    @Override
    public Graph graph() {
        return vertex.graph();
    }

    @Override
    public Object id() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return vertex.id();
    }

    @Override
    public String label() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return property.key();
    }

    @Override
    public <U> Property<U> property(String key, U value) {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return vertex.property(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<? extends Property<?>> propertyIterator(String... keys) {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return vertex.propertyIterator(keys);
    }

    @Override
    public Vertex element() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return vertex;
    }

    @Override
    public com.tinkerpop.gremlin.structure.VertexProperty.Iterators iterators() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return this;
    }
    
    @Override
    public boolean equals(Object vertex) {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        if (!(vertex instanceof SparkseeVertexProperty<?>)) {
            return false;
        }
        VertexProperty<?> otherVertex = (VertexProperty<?>) vertex;
        return this.vertex.equals(otherVertex.element()) && 
                property.key().equals(otherVertex.key()) &&
                property.value().equals(otherVertex.value());
    }
    
    @Override
    public String toString() {
        if (property == null) {
            throw Property.Exceptions.propertyDoesNotExist();
        }
        return StringFactory.propertyString(this);
    }
}
