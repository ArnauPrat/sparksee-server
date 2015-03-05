package com.tinkerpop.gremlin.sparksee.structure;


import com.tinkerpop.gremlin.structure.Graph.Features;
import com.tinkerpop.gremlin.structure.Graph.Features.EdgeFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.EdgePropertyFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.ElementFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.VariableFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.VertexFeatures;
import com.tinkerpop.gremlin.structure.Graph.Features.VertexPropertyFeatures;
import com.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class SparkseeFeatures {

    public static class SparkseeGeneralFeatures implements Features {
        @Override
        public GraphFeatures graph() {
            return new SparkseeGraphFeatures();
        }

        @Override
        public EdgeFeatures edge() {
            return new SparkseeEdgeFeatures();
        }

        @Override
        public VertexFeatures vertex() {
            return new SparkseeVertexFeatures();
        }

        @Override
        public String toString() {
            return StringFactory.featureString(this);
        }
    };

    public static class SparkseeGraphFeatures implements Features.GraphFeatures {
        
        @Override
        public boolean supportsComputer() {
            return false;
        }

        @Override
        public boolean supportsThreadedTransactions() {
            return false;
        }
        
        @Override
        public VariableFeatures variables() {
            return new SparkseeVariableFeatures();
        }
    };
    
    public static class SparkseeVertexFeatures extends SparkseeElementFeatures implements VertexFeatures {
        @Override
        public VertexPropertyFeatures properties() {
            return new SparkseeVertexPropertyFeatures();
        }
        
        @Override
        public boolean supportsMultiProperties() {
            return false;
        }
    };

    public static class SparkseeEdgeFeatures extends SparkseeElementFeatures implements EdgeFeatures {
        @Override
        public EdgePropertyFeatures properties() {
            return new SparkseeEdgePropertyFeatures();
        }
    };
    
    public static class SparkseeVertexPropertyFeatures extends SparkseePropertyFeatures implements VertexPropertyFeatures {
    };
    
    public static class SparkseeEdgePropertyFeatures extends SparkseePropertyFeatures implements EdgePropertyFeatures {
    };
    
    public static class SparkseeElementFeatures implements ElementFeatures {
        @Override
        public boolean supportsUserSuppliedIds() {
            return false;
        }
        
        @Override
        public boolean supportsStringIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }
        
        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }
    };
    
    public static class SparkseePropertyFeatures implements Features.PropertyFeatures {
        @Override
        public boolean supportsFloatValues() {
            return false;
        }
        @Override
        public boolean supportsBooleanArrayValues() {
            return false;
        }

        @Override
        public boolean supportsByteArrayValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleArrayValues() {
            return false;
        }

        @Override
        public boolean supportsFloatArrayValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerArrayValues() {
            return false;
        }

        @Override
        public boolean supportsLongArrayValues() {
            return false;
        }

        @Override
        public boolean supportsMapValues() {
            return false;
        }

        @Override
        public boolean supportsMixedListValues() {
            return false;
        }

        @Override
        public boolean supportsSerializableValues() {
            return false;
        }

        @Override
        public boolean supportsStringArrayValues() {
            return false;
        }

        @Override
        public boolean supportsUniformListValues() {
            return false;
        }
    };
    
    public static class SparkseeVariableFeatures implements Features.VariableFeatures {
        @Override
        public boolean supportsBooleanArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsBooleanValues() {
            return false;
        }

        @Override
        public boolean supportsByteArrayValues() {
            return false;
        }

        @Override
        public boolean supportsByteValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsDoubleValues() {
            return false;
        }

        @Override
        public boolean supportsFloatArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsFloatValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsIntegerValues() {
            return false;
        }

        @Override
        public boolean supportsLongArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsLongValues() {
            return false;
        }

        @Override
        public boolean supportsMapValues() {
            return false;
        }

        @Override
        public boolean supportsMixedListValues() {
            return false;
        }

        @Override
        public boolean supportsSerializableValues() {
            return false;
        }

        @Override
        public boolean supportsStringArrayValues() {
            return false;
        }
        
        @Override
        public boolean supportsStringValues() {
            return false;
        }

        @Override
        public boolean supportsUniformListValues() {
            return false;
        }
    };
}