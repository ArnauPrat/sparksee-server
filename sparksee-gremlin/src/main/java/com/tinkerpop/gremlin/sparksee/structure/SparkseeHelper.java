package com.tinkerpop.gremlin.sparksee.structure;

import java.util.Iterator;

import com.sparsity.sparksee.gdb.EdgesDirection;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;


/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class SparkseeHelper {
    
    public static final int NO_LIMT = Integer.MAX_VALUE;
    
    public static class SparkseeVertexIterator<T extends Vertex> implements Iterator<SparkseeVertex> {

        Graph graph;
        Vertex vertex; 
        EdgesDirection direction;
        String[] labels;
        int limit;
        int index;
        int count;
        com.sparsity.sparksee.gdb.Objects objects;
        com.sparsity.sparksee.gdb.ObjectsIterator iterator;
        
        public SparkseeVertexIterator(SparkseeGraph graph, SparkseeVertex vertex, 
                EdgesDirection direction, int limit, String[] labels) {
            this.graph = graph;
            this.vertex = vertex;
            this.direction = direction;
            this.labels = labels;
            this.limit = limit;
            index = 0;
            count = 0;
            objects = null;
            iterator = null;
            if (labels.length > 0) {
                prepare();
            }
        }

        @Override
        public boolean hasNext() {
            return iterator != null && iterator.hasNext();
        }

        @Override
        public SparkseeVertex next() {
            long id = iterator.next();
            count++;
            prepare();
            com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
            String label = rawGraph.getType(rawGraph.getObjectType(id)).getName();
            return new SparkseeVertex(id, label, (SparkseeGraph) graph);
        }
        
        public void close() {
            if (objects != null) {
                iterator.close();
                ((SparkseeTransaction) graph.tx()).remove(objects);
                objects.close();
                iterator = null;
                objects = null;
            }
        }
        
        private void prepare() {
            if (iterator != null && !iterator.hasNext() && count > limit) {
                iterator.close();
                ((SparkseeTransaction) graph.tx()).remove(objects);
                objects.close();
                iterator = null;
                objects = null;
            }
            
            if (iterator == null) {
                com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
                do {
                    int edgeType = rawGraph.findType(labels[index]);
                    index++;
                    if (edgeType == SparkseeGraph.INVALID_TYPE) {
                        continue;
                    }
                    objects = rawGraph.neighbors((Long) vertex.id(), edgeType, direction);
                    ((SparkseeTransaction) graph.tx()).add(objects);
                } while (objects != null && index < labels.length);
                if (objects != null) {
                    iterator = objects.iterator();
                }
            }
        }
    };
    
    public static class SparkseeEdgeIterator<T extends Edge> implements Iterator<SparkseeEdge> {

        Graph graph;
        Vertex vertex; 
        EdgesDirection direction;
        String[] labels;
        int limit;
        int index;
        int count;
        com.sparsity.sparksee.gdb.Objects objects;
        com.sparsity.sparksee.gdb.ObjectsIterator iterator;
        
        public SparkseeEdgeIterator(SparkseeGraph graph, SparkseeVertex vertex, 
                EdgesDirection direction, int limit, String[] labels) {
            this.graph = graph;
            this.vertex = vertex;
            this.direction = direction;
            this.labels = labels;
            this.limit = limit;
            index = 0;
            count = 0;
            objects = null;
            iterator = null;
            if (labels.length > 0) {
                prepare();
            }
        }

        @Override
        public boolean hasNext() {
            return iterator != null;
        }

        @Override
        public SparkseeEdge next() {
            long id = iterator.next();
            count++;
            prepare();
            com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
            String label = rawGraph.getType(rawGraph.getObjectType(id)).getName();
            return new SparkseeEdge(id, label, (SparkseeGraph) graph);
        }
        
        public void close() {
            if (objects != null) {
                iterator.close();
                ((SparkseeTransaction) graph.tx()).remove(objects);
                objects.close();
                iterator = null;
                objects = null;
            }
        }
        
        private void prepare() {
            if (count > limit || (iterator != null && !iterator.hasNext())) {
                iterator.close();
                ((SparkseeTransaction) graph.tx()).remove(objects);
                objects.close();
                iterator = null;
                objects = null;
            }
            
            if (iterator == null && count < limit) {
                com.sparsity.sparksee.gdb.Graph rawGraph = ((SparkseeTransaction) graph.tx()).getRawGraph();
                while (objects == null && index < labels.length) {
                    int edgeType = rawGraph.findType(labels[index++]);
                    if (edgeType == SparkseeGraph.INVALID_TYPE) {
                        continue;
                    }
                    objects = rawGraph.explode((Long) vertex.id(), edgeType, direction);
                    if (objects.size() == 0) {
                        objects.close();
                        objects = null;
                    }
                }
                if (objects != null) {
                    ((SparkseeTransaction) graph.tx()).add(objects);
                    iterator = objects.iterator();
                }
            }
        }
    };
}
