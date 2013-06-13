import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import sun.awt.windows.ThemeReader;



/*
import weiss.util.Iterator;
import weiss.util.Collection;
import weiss.util.List;
import weiss.util.Queue;
import weiss.util.Map;
import weiss.util.LinkedList;
import weiss.util.HashMap;
import weiss.util.NoSuchElementException;
import weiss.util.PriorityQueue;

import weiss.nonstandard.PairingHeap;
*/

// Used to signal violations of preconditions for
// various shortest path algorithms.
class GraphException extends RuntimeException
{
    public GraphException( String name )
    {
        super( name );
    }
}

// Represents an edge in the graph.
class Edge implements Comparable
{
    public Vertex     dest;   // Second vertex in Edge
    public double     cost;   // Edge cost
    
    public Edge( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		double result = this.cost - ((Edge)o).cost;
		
		if(result < 0)
			return -1;
		else if(result > 0)
			return 1;
		else
			return 0;
	}
}

//Define a ref edge
class refEdge implements Comparable<refEdge>{
	
	private Vertex s;
	private Vertex e;
	private double dis;
	
	public refEdge(Vertex start, Vertex end, double cost){
		s = start;
		e = end;
		dis = cost;
	}
	
	public Vertex getStart(){
		return s;
	}
	
	public Vertex getEnd(){
		return e;
	}
	
	public double getCost(){
		return dis;
	}

	@Override
	public int compareTo(refEdge o) {
		// TODO Auto-generated method stub
		if((this.dis - o.dis) < 0)
			return -1;
		else if((this.dis - o.dis) > 0)
			return 1;
		else {
			return 0;
		}
	}
	
}

// Represents an entry in the priority queue for Dijkstra's algorithm.
class Path implements Comparable<Path>
{
    public Vertex     dest;   // w
    public double     cost;   // d(w)
    
    public Path( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
    
    public int compareTo( Path rhs )
    {
        double otherCost = rhs.cost;
        
        return cost < otherCost ? -1 : cost > otherCost ? 1 : 0;
    }
}

// Represents a vertex in the graph.
class Vertex
{
    public String     name;   // Vertex name
    public List<Edge> adj;    // Adjacent vertices
    public double     dist;   // Cost
    public Vertex     prev;   // Previous vertex on shortest path
    public int        scratch;// Extra variable used in algorithm

    public Vertex( String nm )
      { name = nm; adj = new LinkedList<Edge>( ); reset( ); }

    public void reset( )
      { dist = Graph.INFINITY; prev = null; pos = null; scratch = 0; }    
      
    public PairingHeap.Position<Path> pos;  // Used for dijkstra2 (Chapter 23)
    
    //---------------implements equals---------------------
    
    public boolean equals(Object o){
    	return this.name.equals(((Vertex)o).name);
    }
    
    //-------------------end-------------------------------
}

// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
//                              --> Add additional edge
// void printPath( String w )   --> Print path after alg is run
// void unweighted( String s )  --> Single-source unweighted
// void dijkstra( String s )    --> Single-source weighted
// void negative( String s )    --> Single-source negative weighted
// void acyclic( String s )     --> Single-source acyclic
// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm.  Exceptions are thrown if errors are detected.

public class Graph
{
    public static final double INFINITY = Double.MAX_VALUE;
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>( );
    
    //-------------------define helper function to the edge----------------------
    
    public PriorityQueue<refEdge> edgePool = new PriorityQueue<refEdge>();
    
    
    
    //------------------------------end------------------------------------------

    /**
     * Add a new edge to the graph.
     */
    public void addEdge( String sourceName, String destName, double cost )
    {
        Vertex v = getVertex( sourceName );
        Vertex w = getVertex( destName );
        v.adj.add( new Edge( w, cost ) );
        //----------------make a refEdge and put it in the edgePool-----------------
        refEdge rE = new refEdge(v, w, cost);
        edgePool.add(rE);
        //------------------------------end-----------------------------------------
    }

    /**
     * Driver routine to handle unreachables and print total cost.
     * It calls recursive routine to print shortest path to
     * destNode after a shortest path algorithm has run.
     */
    public void printPath( String destName )
    {
        Vertex w = vertexMap.get( destName );
        if( w == null )
            throw new NoSuchElementException( "Destination vertex not found" );
        else if( w.dist == INFINITY )
            System.out.println( destName + " is unreachable" );
        else
        {
            System.out.print( "(Cost is: " + w.dist + ") " );
            printPath( w );
            System.out.println( );
        }
    }

    /**
     * If vertexName is not present, add it to vertexMap.
     * In either case, return the Vertex.
     */
    private Vertex getVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if( v == null )
        {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     * Recursive routine to print shortest path to dest
     * after running shortest path algorithm. The path
     * is known to exist.
     */
    private void printPath( Vertex dest )
    {
        if( dest.prev != null )
        {
            printPath( dest.prev );
            System.out.print( " to " );
        }
        System.out.print( dest.name );
    }
    
    /**
     * Initializes the vertex output info prior to running
     * any shortest path algorithm.
     */
    private void clearAll( )
    {
        for( Vertex v : vertexMap.values( ) )
            v.reset( );
    }

    /**
     * Single-source unweighted shortest-path algorithm.
     */
    public void unweighted( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                if( w.dist == INFINITY )
                {
                    w.dist = v.dist + 1;
                    w.prev = v;
                    q.add( w );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm.
     */
    public void dijkstra( String startName )
    {
        PriorityQueue<Path> pq = new PriorityQueue<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        pq.add( new Path( start, 0 ) ); start.dist = 0;
        
        int nodesSeen = 0;
        while( !pq.isEmpty( ) && nodesSeen < vertexMap.size( ) )
        {
            Path vrec = pq.remove( );
            Vertex v = vrec.dest;
            if( v.scratch != 0 )  // already processed v
                continue;
                
            v.scratch = 1;
            nodesSeen++;

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist +cvw;
                    w.prev = v;
                    pq.add( new Path( w, w.dist ) );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm using pairing heaps.
     */
    public void dijkstra2( String startName )
    {
        PairingHeap<Path> pq = new PairingHeap<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        start.pos = pq.insert( new Path( start, 0 ) ); start.dist = 0;

        while ( !pq.isEmpty( ) )
        {
            Path vrec = pq.deleteMin( );
            Vertex v = vrec.dest;
                
            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                    
                    Path newVal = new Path( w, w.dist );                    
                    if( w.pos == null )
                        w.pos = pq.insert( newVal );
                    else
                        pq.decreaseKey( w.pos, newVal ); 
                }
            }
        }
    }
    
    /**
     * Prim algorithm.
     */
    public void prim(String startName, Graph inputGraph){
    	Queue<Vertex> q = new LinkedList<Vertex>();
    	LinkedList<Vertex> explored = new LinkedList<Vertex>();
    	
    	LinkedList<refEdge> primTree = new LinkedList<refEdge>();
    	LinkedList<String> nodeContainer = new LinkedList<String>();
    	Stack<String> bookKeeper = new Stack<String>();
    	
    
    	
    	
    	Vertex start = vertexMap.get(startName);
    	
    	if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );
    	/*
    	for(Edge e : start.adj){
    		System.out.println(e.dest.name + " " + e.cost);
    	}
    	*/
    	
    	q.add(start);
    	
    	while(!q.isEmpty()){
    		Vertex popV = q.poll();
    		explored.add(popV);
    		
    		for(Edge e : popV.adj){
    			if(!explored.contains(e.dest))
    				q.add(e.dest);
    		}
    	}
    	
    	
 
    	Iterator it = explored.iterator();
    	while(it.hasNext()){
    		Vertex node = (Vertex)it.next();
    		
    		
    		System.out.println(node.name);
    		nodeContainer.add(node.name);
    		
    		
    		/*
    		for(Edge e: node.adj){
    			System.out.println(node.name + " " + e.dest.name);
    			if((!exploredStart.contains(node) && !exploredDest.contains(e.dest)) ||
    					(!exploredStart.contains(e.dest) && !exploredDest.contains(node))){
    				refEdge refedge = new refEdge(node, e.dest, e.cost);
    				refEdgeContainer.add(refedge);
    				exploredStart.add(node);
    				exploredDest.add(e.dest);
    			}
    			
    		}
    		*/
    		
    		
    	}
    	
    	System.out.println("There are edges:         ");
    	Iterator<refEdge> refEdgeIterator = inputGraph.edgePool.iterator();
    	
    	while(refEdgeIterator.hasNext()){
    		refEdge ref = (refEdge)refEdgeIterator.next();
    		System.out.println(ref.getStart().name + "-" + ref.getEnd().name + ":" + ref.getCost());
    	}
    	
    	/*
    	while(!inputGraph.edgePool.isEmpty()){
    		System.out.print(((refEdge)inputGraph.edgePool.poll()).getCost() + " ");
    	}
    	*/
    		
    	
    	
    	System.out.println("---------------------------------------------------");
    	
    	Object[] sortListEdges = inputGraph.edgePool.toArray();
    	Arrays.sort(sortListEdges);
    	
    	for(int i = 0; i < sortListEdges.length; i++){
    		System.out.println(((refEdge)sortListEdges[i]).getStart().name + "-" + ((refEdge)sortListEdges[i]).getEnd().name + ":" + ((refEdge)sortListEdges[i]).getCost());
    	}
    	
    	
    	
    	refEdge popRefEdge = inputGraph.edgePool.poll();
    	String node1 = popRefEdge.getStart().name;
    	String node2 = popRefEdge.getEnd().name;
    	bookKeeper.add(node1);
    	bookKeeper.add(node2);
    	primTree.add(popRefEdge);
    	
    	
    	
    	while(bookKeeper.size() != nodeContainer.size()){
    		for(int i = 0; i < inputGraph.edgePool.size(); i++){
    			
    		}
    	}
    	
    	
    	
    	
    	
    	
    }
    
    public Vertex getMinCostNeighbor(Vertex v){
    	PriorityQueue<Edge> neighborEdges = new PriorityQueue<Edge>();
    	
    	for(Edge e : v.adj){
    		neighborEdges.add(e);
    	}
    	
    	return neighborEdges.remove().dest;
    }

    /**
     * Single-source negative-weighted shortest-path algorithm.
     */
    public void negative( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0; start.scratch++;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );
            if( v.scratch++ > 2 * vertexMap.size( ) )
                throw new GraphException( "Negative cycle detected" );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                      // Enqueue only if not already on the queue
                    if( w.scratch++ % 2 == 0 )
                        q.add( w );
                    else
                        w.scratch--;  // undo the enqueue increment    
                }
            }
        }
    }

    /**
     * Single-source negative-weighted acyclic-graph shortest-path algorithm.
     */
    public void acyclic( String startName )
    {
        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( ); 
        Queue<Vertex> q = new LinkedList<Vertex>( );
        start.dist = 0;
        
          // Compute the indegrees
		Collection<Vertex> vertexSet = vertexMap.values( );
        for( Vertex v : vertexSet )
            for( Edge e : v.adj )
                e.dest.scratch++;
            
          // Enqueue vertices of indegree zero
        for( Vertex v : vertexSet )
            if( v.scratch == 0 )
                q.add( v );
       
        int iterations;
        for( iterations = 0; !q.isEmpty( ); iterations++ )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( --w.scratch == 0 )
                    q.add( w );
                
                if( v.dist == INFINITY )
                    continue;    
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                }
            }
        }
        
        if( iterations != vertexMap.size( ) )
            throw new GraphException( "Graph has a cycle!" );
    }

    /**
     * Process a request; return false if end of file.
     */
    public static boolean processRequest( BufferedReader in, Graph g )
    {
        String startName = null;
        String destName = null;
        String alg = null;

        try
        {
            System.out.print( "Enter start node:" );
            if( ( startName = in.readLine( ) ) == null )
                return false;
            System.out.print( "Enter destination node:" );
            if( ( destName = in.readLine( ) ) == null )
                return false;
            System.out.print( " Enter algorithm (u, d, n, a, p ): " );   
            if( ( alg = in.readLine( ) ) == null )
                return false;
            
            if( alg.equals( "u" ) ){
                g.unweighted( startName );
                
                g.printPath( destName );
            }
            else if( alg.equals( "d" ) )    
            {
                g.dijkstra( startName );
                g.printPath( destName );
                g.dijkstra2( startName );
                
                g.printPath( destName );
            }
            else if( alg.equals( "n" ) ){
                g.negative( startName );
                
                g.printPath( destName );
            }
            else if( alg.equals( "a" ) ){
                g.acyclic( startName );
                
                g.printPath( destName );
            }
            //----below is for prim's algorithm---------
            else if(alg.endsWith("p"))
            	g.prim(startName, g);
            //--------------end------------------------- 
            
            //g.printPath( destName );
        }
        catch( IOException e )
          { System.err.println( e ); }
        catch( NoSuchElementException e )
          { System.err.println( e ); }          
        catch( GraphException e )
          { System.err.println( e ); }
        return true;
    }

    /**
     * A main routine that:
     * 1. Reads a file containing edges (supplied as a command-line parameter);
     * 2. Forms the graph;
     * 3. Repeatedly prompts for two vertices and
     *    runs the shortest path algorithm.
     * The data file is a sequence of lines of the format
     *    source destination.
     */
    public static void main( String [ ] args )
    {
        Graph g = new Graph( );
        try
        {
            FileReader fin = new FileReader( args[0] );
            BufferedReader graphFile = new BufferedReader( fin );

            // Read the edges and insert
            String line;
            while( ( line = graphFile.readLine( ) ) != null )
            {
                StringTokenizer st = new StringTokenizer( line );

                try
                {
                    if( st.countTokens( ) != 3 )
                    {
                        System.err.println( "Skipping ill-formatted line " + line );
                        continue;
                    }
                    String source  = st.nextToken( );
                    String dest    = st.nextToken( );
                    int    cost    = Integer.parseInt( st.nextToken( ) );
                    g.addEdge( source, dest, cost );
                }
                catch( NumberFormatException e )
                  { System.err.println( "Skipping ill-formatted line " + line ); }
             }
         }
         catch( IOException e )
           { System.err.println( e ); }

         System.out.println( "File read..." );
         System.out.println( g.vertexMap.size( ) + " vertices" );

         BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
         while( processRequest( in, g ) )
             ;
    }
}