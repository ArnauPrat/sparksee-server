package com.tinkerpop.gremlin.sparksee.structure;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.sparsity.sparksee.gdb.Attribute;
import com.sparsity.sparksee.gdb.AttributeKind;
import com.sparsity.sparksee.gdb.AttributeList;
import com.sparsity.sparksee.gdb.DataType;
import com.sparsity.sparksee.gdb.TextStream;
import com.sparsity.sparksee.gdb.Value;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.util.ElementHelper;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public abstract class SparkseeElement implements Element, Element.Iterators {

    protected static final int INVALID_ATTRIBUTE = com.sparsity.sparksee.gdb.Attribute.InvalidAttribute;
    
    protected final Long id;
    protected final String label;
    protected final SparkseeGraph graph;

    protected SparkseeElement(final Long id, final String label, final SparkseeGraph graph) {
        this.graph = graph;
        this.id = id;
        this.label = label;
    }

    @SuppressWarnings("rawtypes")
    private  Property<?> getProperty(com.sparsity.sparksee.gdb.Graph rawGraph, int attrId) {
        Attribute attribute = rawGraph.getAttribute(attrId);
        String name = attribute.getName();
        Value v = (attribute.getDataType() != com.sparsity.sparksee.gdb.DataType.Text) ?
                rawGraph.getAttribute(id, attrId) : null;
        if (v != null && v.isNull()) {
            return Property.empty();
        }
        switch (attribute.getDataType()) {
        case Boolean:
            return new SparkseeProperty<Boolean>(graph, this, name, attrId, v.getBoolean());
        case Double:
            return new SparkseeProperty<Double>(graph, this, name, attrId, v.getDouble());
        case Integer:
            return new SparkseeProperty<Integer>(graph, this, name, attrId, v.getInteger());
        case Long:
            return new SparkseeProperty<Long>(graph, this, name, attrId, v.getLong());
        case OID:
            return new SparkseeProperty<Long>(graph, this, name, attrId, v.getOID());
        case String:
            return new SparkseeProperty<String>(graph, this, name, attrId, v.getString());
        case Timestamp:
            return new SparkseeProperty<Long>(graph, this, name, attrId, v.getTimestamp());
        case Text:
            TextStream stream = rawGraph.getAttributeText(id, attrId);
            StringBuilder builder = new StringBuilder(1024);
            int read = 0;
            do {
                char[] buffer = new char[1024];
                read = stream.read(buffer, 1024);
                builder.append(buffer);
            } while (read > 0);
            return new SparkseeProperty<String>(graph, this, name, attrId, builder.toString());
        default:
            return (SparkseeProperty) Property.empty();
        }
    }
    
    @Override
    public Object id() {
        return id;
    }
    
    @Override
    public String label() {
        return label;
    }
    
    public void remove() {
        graph.tx().readWrite();
        try {
            ((SparkseeTransaction) graph.tx()).getRawGraph().drop((Long) id);
        } catch (Exception e) {
            throw Element.Exceptions.elementAlreadyRemoved(SparkseeElement.class, id);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Property<?> property(final String key) {
        try {
            this.graph.tx().readWrite();
            com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();

            int attributeType = SparkseeGraph.INVALID_TYPE;
            if (this instanceof SparkseeVertex) {
                attributeType = SparkseeVertex.SCOPE;
            } else if (this instanceof SparkseeEdge) {
                attributeType = SparkseeEdge.SCOPE;
            }

            int attrId = rawGraph.findAttribute(attributeType, key);

            if (attrId == INVALID_ATTRIBUTE) {
                return Property.empty();
            }

            return getProperty(rawGraph, attrId);
        } catch (Exception e) {
            throw Element.Exceptions.elementAlreadyRemoved(Element.class, id);
        }
    }
    
    @Override
    public <V> Property<V> property(final String key, final V value) {
        ElementHelper.validateProperty(key, value);
        ((SparkseeTransaction)this.graph.tx()).write();
        this.graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        
        int attributeType = SparkseeGraph.INVALID_TYPE;
        if (this instanceof SparkseeVertex) {
            attributeType = SparkseeVertex.SCOPE;
        } else if (this instanceof SparkseeEdge) {
            attributeType = SparkseeEdge.SCOPE;
        }
        
        int attrId = rawGraph.findAttribute(attributeType, key);
        
        Value v = new Value();
        if (value instanceof Boolean) {
            if (attrId == INVALID_ATTRIBUTE) {
                attrId = rawGraph.newAttribute(attributeType, key, DataType.Boolean, AttributeKind.Basic);
            }
            v.setBoolean((Boolean) value);
        } else if (value instanceof Integer || value instanceof Byte) {
            if (attrId == INVALID_ATTRIBUTE) {
                attrId = rawGraph.newAttribute(attributeType, key, DataType.Integer, AttributeKind.Basic);
            }
            v.setInteger((Integer) value);
        } else if (value instanceof Long) {
            if (attrId == INVALID_ATTRIBUTE) {
                attrId = rawGraph.newAttribute(attributeType, key, DataType.Long, AttributeKind.Basic);
            }
            v.setLong((Long) value);
        } else if (value instanceof String) {
            if (attrId == INVALID_ATTRIBUTE) {
                attrId = rawGraph.newAttribute(attributeType, key, DataType.String, AttributeKind.Basic);
            }
            v.setString((String) value);
        } else if (value instanceof Double) {
            if (attrId == INVALID_ATTRIBUTE) {
                attrId = rawGraph.newAttribute(attributeType, key, DataType.Double, AttributeKind.Basic);
            }
            v.setDouble((Double)value);
        } else {
            throw Property.Exceptions.dataTypeOfPropertyValueNotSupported(value);
        }
        
        try {
            rawGraph.setAttribute(id, attrId, v);
            return new SparkseeProperty<>(graph, this, key, attrId, value);
        } catch (Exception e) {
            throw Element.Exceptions.elementAlreadyRemoved(Element.class, id);
        }
    }
    
    @Override
    public Graph graph() {
        return graph;
    }
    
    @Override
    public Set<String> keys() {
        this.graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        AttributeList attrList = rawGraph.getAttributes(id);
        final Set<String> keys = new HashSet<>();
        for (int attrId : attrList) {
            Attribute attribute = rawGraph.getAttribute(attrId);
            if (!Graph.Key.isHidden(attribute.getName())) {
                keys.add(attribute.getName());
            }
        }
        return keys;
    }

    public Map<String, Property<?>> properties() {
        graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        AttributeList attrList = rawGraph.getAttributes(id);
        Map<String, Property<?>> map = new HashMap<String, Property<?>>();
        for (int attrId : attrList) {
            Property<?> property = getProperty(rawGraph, attrId);
//            if (!property.isHidden()) {
                map.put(property.key(), property);
//            }
        }
        return map;
    }
    
    
    public Set<String> hiddenKeys() {
        this.graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        AttributeList attrList = rawGraph.getAttributes(id);
        final Set<String> keys = new HashSet<>();
        for (int attrId : attrList) {
            Attribute attribute = rawGraph.getAttribute(attrId);
            if (Graph.Key.isHidden(attribute.getName())) {
                keys.add(attribute.getName());
            }
        }
        return keys;
    }
    
    public Map<String, Property<?>> hiddens() {
        graph.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
        AttributeList attrList = rawGraph.getAttributes(id);
        Map<String, Property<?>> map = new HashMap<String, Property<?>>();
        for (int attrId : attrList) {
            Property<?> property = getProperty(rawGraph, attrId);
            if (property.isHidden()) {
                map.put(Graph.Key.unHide(property.key()), property);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public Iterator<? extends Property<?>> hiddenPropertyIterator(String... keys) {
        Map<String, Property<?>> propertyMap = hiddens();
        return (Iterator<? extends Property<?>>) propertyMap.values().stream().filter(property -> keys.length == 0 ||
                Stream.of(keys).filter(k -> k.equals(property.key())).findAny().isPresent());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<? extends Property<?>> propertyIterator(String... keys) {
        Map<String, Property<?>> propertyMap = properties();
        return (Iterator<? extends Property<?>>) propertyMap.values().stream().filter(property -> keys.length == 0 ||
                Stream.of(keys).filter(k -> k.equals(property.key())).findAny().isPresent()).iterator();
    }
    
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}
