package com.tinkerpop.gremlin.sparksee.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.log4j.InstrumentedAppender;
import com.tinkerpop.gremlin.process.computer.GraphComputer;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.util.DefaultGraphTraversal;
import com.tinkerpop.gremlin.sparksee.process.graph.step.sideEffect.SparkseeGraphStep;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Transaction;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_STRUCTURE_PERFORMANCE)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_STANDARD)
@Graph.OptIn(Graph.OptIn.SUITE_PROCESS_COMPUTER)
public class SparkseeGraph implements Graph,SparkseeGraphMBean {
    
    protected static final int INVALID_TYPE = com.sparsity.sparksee.gdb.Type.InvalidType;
    
    private static final String DB_PARAMETER     = "gremlin.sparksee.directory";
    private static final String CONFIG_DIRECTORY = "gremlin.sparksee.config";
    private static final String PYTHON_FROM_IMPORTS = "gremlin.sparksee.pythonfromimports";
    private static final String PYTHON_ALIAS = "gremlin.sparksee.pythonalias";
    
    /**
     * Database persistent file.
     */
    private File dbFile = null;
    private com.sparsity.sparksee.gdb.Sparksee sparksee = null;
    private com.sparsity.sparksee.gdb.Database db = null;
    private SparkseeTransaction transaction = null;
    private Configuration configuration;
    
    private final static Counter requestQueries;
    private final static Counter processedQueries;
    
    private String licenseCode = null;
    private String dabaseFile = null;
    private String pythonHeader = "";
   
    private static final Logger LOG = LoggerFactory.getLogger(SparkseeGraph.class);
    
    static{
    	MetricRegistry registry = new MetricRegistry();
    	InstrumentedAppender appender = new InstrumentedAppender(registry);
    	appender.activateOptions();
    	LogManager.getRootLogger().addAppender(appender);
    	
    	
		JmxReporter reporter = JmxReporter.forRegistry(registry).build();
		reporter.start();
		
		requestQueries = registry.counter("request-queries");
		processedQueries = registry.counter("processed-queries");
		
    }
    
    public String getLicense(){
    	return licenseCode;
    }
    
    public String getDatabaseFile(){
    	return dabaseFile;
    }
    
    public com.sparsity.sparksee.gdb.Database getRawDatabase() {
    	return db;
    }
     
    private SparkseeGraph(final Configuration configuration) {
        this.configuration = configuration;

        final String fileName   = configuration.getString(DB_PARAMETER);
        final String configFile = configuration.getString(CONFIG_DIRECTORY, null);
        dabaseFile = fileName;
        

        dbFile = new File(fileName);

        if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
            throw new InvalidParameterException(String.format("Unable to create directory %s.", dbFile.getParent()));
        }
        
        try {
            if (configFile != null) {
            	
            	Properties prop = new Properties();
        		InputStream input = null;

        		input = new FileInputStream(configFile);
        		prop.load(input);
        		
        		licenseCode = prop.getProperty("sparksee.license");
        			
        		if (input != null) {
        			input.close();
        		}

                com.sparsity.sparksee.gdb.SparkseeProperties.load(configFile);
            }

            sparksee = new com.sparsity.sparksee.gdb.Sparksee(new com.sparsity.sparksee.gdb.SparkseeConfig());
            if (!dbFile.exists()) {
                db = sparksee.create(dbFile.getPath(), dbFile.getName());
            } else {
                db = sparksee.open(dbFile.getPath(), false);
            }
            transaction = new SparkseeTransaction(this, db);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        final String pythonFromImportsFile = configuration.getString(PYTHON_FROM_IMPORTS, null);
        if (pythonFromImportsFile != null) {
        	
        	final String pythonAliasFile = configuration.getString(PYTHON_ALIAS, null);
        	
        	try {
        		pythonHeader = "";
				BufferedReader br = new BufferedReader(new FileReader(pythonFromImportsFile));
				String line = br.readLine();
				while(line!=null){
					pythonHeader = pythonHeader + line +"\n";
					line = br.readLine();
				}
				br.close();
				
				br = new BufferedReader(new FileReader(pythonAliasFile));
				line = br.readLine();
				while(line!=null){
					pythonHeader = pythonHeader + line +"\n";
					line = br.readLine();
				}
				br.close();
				
        	} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
        }
    }
    
    /**
     * Open a new {@link SparkseeGraph} instance.
     *
     * @param configuration the configuration for the instance
     * @param <G>           the {@link com.tinkerpop.gremlin.structure.Graph} instance
     * @return a newly opened {@link com.tinkerpop.gremlin.structure.Graph}
     */
    @SuppressWarnings("unchecked")
    public static <G extends Graph> G open(final Configuration configuration) {

    
        if (configuration == null) {
            throw Graph.Exceptions.argumentCanNotBeNull("configuration");
        }
        if (!configuration.containsKey(DB_PARAMETER)) {
            throw new IllegalArgumentException(String.format("Sparksee configuration requires %s to be set", DB_PARAMETER));
        }
    	LOG.debug("opening "+configuration.getString(DB_PARAMETER));
        SparkseeGraph graph = new SparkseeGraph(configuration);
        
        class Multi extends Thread{  
        	public void run(){  
        		
        		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				try {
					// Construct the ObjectName for the MBean we will register
					ObjectName name = new ObjectName("com.tinkerpop.gremlin.sparksee.structure:type=SparkseeGraph");

					// Register the Hello World MBean
					mbs.registerMBean(graph, name);
				
					// Wait forever
					Thread.sleep(Long.MAX_VALUE);
				} catch (MalformedObjectNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (InstanceAlreadyExistsException
					| MBeanRegistrationException
					| NotCompliantMBeanException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        Multi t1=new Multi();  
        t1.start();  
        
        G g = (G) graph;
        
        return g;
    }
    
    public void changeDBPath(String path){
    	
    	this.close();
    }

    public String compute(String algebra,Map<String, Object> params) {
    	requestQueries.inc();
      this.tx().readWrite();
      try {
    	  LOG.debug("new query: "+algebra);
    	  LOG.debug("new query: "+params.toString());
    	  Object laguageObj = params.get("query-lang");
    	  if(laguageObj!=null && ((String)laguageObj).equals("python") ){
    		  ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
    		  engine.put("db", getRawDatabase());
    		  Object codeObj = params.get("query-code");
    		  if(codeObj!=null){
    			  engine.eval(pythonHeader+(String)codeObj);
    		  }
    		  return "{\"id\":"+0+"}";
    	  }
    	  else {
    		  Integer queryId = ((SparkseeTransaction) this.tx()).newQuery(algebra,params);//, params);
    		  return "{\"id\":"+queryId.toString()+"}";
    	  }
      } catch (Exception e) {
          return e.getMessage();
      }
    }

    public String next(Long queryId, Long rows) {
        this.tx().readWrite();
        return ((SparkseeTransaction) this.tx()).next(queryId.intValue(), rows.intValue());
    }

    public String closeQuery(Long queryId) {
        this.tx().readWrite();
        String closeRequest = ((SparkseeTransaction) this.tx()).closeQuery(queryId.intValue());
        if(closeRequest.equals("{}")){
        	processedQueries.inc();
        }
        return closeRequest;
    }
    
    @Override
    public Vertex addVertex(final Object... keyValues) {
        ElementHelper.legalPropertyKeyValueArray(keyValues);
        if (ElementHelper.getIdValue(keyValues).isPresent()) {
            throw Vertex.Exceptions.userSuppliedIdsNotSupported();
        }
        final String label = ElementHelper.getLabelValue(keyValues).orElse(Vertex.DEFAULT_LABEL);
        ElementHelper.validateLabel(label);

        ((SparkseeTransaction)this.tx()).write();
        this.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = transaction.getRawGraph();
        int type = rawGraph.findType(label);
        if (type == INVALID_TYPE) {
            type = rawGraph.newNodeType(label);
        }
        assert type != INVALID_TYPE;
        
        long oid = rawGraph.newNode(type);
        final SparkseeVertex vertex = new SparkseeVertex(oid, label, this);
        ElementHelper.attachProperties(vertex, keyValues);
        return vertex;
    }

    @Override
    public Vertex v(final Object id) {
        if (id == null) {
            throw Graph.Exceptions.elementNotFound(Vertex.class, id);
        }
        
        this.tx().readWrite();
        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            com.sparsity.sparksee.gdb.Graph rawGraph = transaction.getRawGraph();
            final int type = rawGraph.getObjectType(longId);
            if (type == INVALID_TYPE) {
                throw Graph.Exceptions.elementNotFound(Vertex.class, id);
            }
            
            return new SparkseeVertex(longId, rawGraph.getType(type).getName(), this);
        } catch (Exception e) {
            throw Graph.Exceptions.elementNotFound(Vertex.class, id);
        }
    }

    @Override
    public Edge e(final Object id) {
        if (id == null) {
            throw Graph.Exceptions.elementNotFound(Vertex.class, id);
        }
        
        this.tx().readWrite();
        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            com.sparsity.sparksee.gdb.Graph rawGraph = transaction.getRawGraph();
            final int type = rawGraph.getObjectType(longId);
            if (type == INVALID_TYPE) {
                throw Graph.Exceptions.elementNotFound(Vertex.class, id);
            }
            
            return new SparkseeEdge(longId, rawGraph.getType(type).getName(), this);
        } catch (Exception e) {
            throw Graph.Exceptions.elementNotFound(Vertex.class, id);
        }
    }
    
    public Iterator<SparkseeVertex> vertices() {
        ArrayList<SparkseeVertex> list = new ArrayList<SparkseeVertex>();
        this.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = transaction.getRawGraph();
        com.sparsity.sparksee.gdb.TypeList typeList = rawGraph.findTypes();
        for (Integer type : typeList) {
            if (rawGraph.getType(type).getObjectType() == com.sparsity.sparksee.gdb.ObjectType.Edge) {
                continue;
            }
            com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
            com.sparsity.sparksee.gdb.ObjectsIterator objsIterator = objs.iterator();
            while (objsIterator.hasNext()) {
                long oid = objsIterator.next();
                list.add(new SparkseeVertex(oid, rawGraph.getType(type).getName(), this));
            }
            objsIterator.close();
            objs.close();
        }
        return list.iterator();
    }

    public Iterator<SparkseeEdge> edges() {
        ArrayList<SparkseeEdge> list = new ArrayList<SparkseeEdge>();
        this.tx().readWrite();
        com.sparsity.sparksee.gdb.Graph rawGraph = transaction.getRawGraph();
        com.sparsity.sparksee.gdb.TypeList typeList = rawGraph.findTypes();
        for (Integer type : typeList) {
            if (rawGraph.getType(type).getObjectType() == com.sparsity.sparksee.gdb.ObjectType.Node) {
                continue;
            }
            com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
            com.sparsity.sparksee.gdb.ObjectsIterator objsIterator = objs.iterator();
            while (objsIterator.hasNext()) {
                long oid = objsIterator.next();
                list.add(new SparkseeEdge(oid, rawGraph.getType(type).getName(), this));
            }
            objsIterator.close();
            objs.close();
        }
        return list.iterator();
    }
    
    @Override
    public void close() {
        transaction.closeAll();
        db.close();
        sparksee.close();
    }

    @Override
    public Transaction tx() {
        return transaction;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public GraphComputer compute(final Class... graphComputerClass) {
        throw Graph.Exceptions.graphComputerNotSupported();
    }
    
    @Override
    public Features features() {
        return new SparkseeFeatures.SparkseeGeneralFeatures();
    }

    @Override
    public Variables variables() {
        throw Graph.Exceptions.variablesNotSupported();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, dbFile.getPath());
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public GraphTraversal<Vertex, Vertex> V() {
        tx().readWrite();
        GraphTraversal<Vertex, Vertex> traversal = new DefaultGraphTraversal<Vertex, Vertex>(this);
        traversal.addStep(new SparkseeGraphStep<Vertex>(traversal, Vertex.class, this));
        return traversal;
    }

    @Override
    public GraphTraversal<Edge, Edge> E() {
        tx().readWrite();
        GraphTraversal<Edge, Edge> traversal = new DefaultGraphTraversal<Edge, Edge>(this);
        traversal.addStep(new SparkseeGraphStep<Edge>(traversal, Edge.class, this));
        return traversal;
    }
}
