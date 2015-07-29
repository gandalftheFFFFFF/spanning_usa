import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpanningUSA {
	
	public static void main(String[] args) {
		
		// Read input in main and store in Edge objects
		
		// Pattern to recognize the distances in the form [123]
		Pattern p = Pattern.compile("\\[\\d*\\]");
		
		// Read the whole file only selecting lines that contain distances,
		// and store Edges in a priority queue
		Queue<Edge> q = new PriorityQueue<Edge>();
		
		while(StdIn.hasNextLine()) {
			
			String line = StdIn.readLine();
			Matcher m = p.matcher(line);
			
			if (m.find()) {
				
				String d = m.group().replaceAll("\\[|\\]", ""); // Get rid of square brackets around the distance
				line = line.replaceAll("\\[\\d*\\]|\"", "").trim(); // Get rid of distances and square brackets as well as leading and trailing whitespace
				q.add(new Edge(line.split("--"), Integer.parseInt(d)));
			}
		}
		
		// Now we have all the weights sorted and we can start added them together, while we create the spanning tree.
		// We check for loops by doing a depth-first search.
		
		Graph graph = new Graph();
		
		while (!q.isEmpty()) {
			// Add an edge from top of the queue
			Edge e = q.poll();
			
			// Check for loop and add or don't add
			if (!graph.isCyclic(e)) {
				// isCyclic adds the edge if it isn't cyclic. This implementation could be a lot better!
				
				// Add the edge to a container to keep track of spanning tree edges
				graph.addMSTEdge(e);
			}
		}
		
		// Get the MST value from the graph
		System.out.println(graph.getMST());
		
	}
	
}

@SuppressWarnings("serial")
class Graph extends HashMap<String, LinkedList<String>> {
	
	/*
	 * The Graph class is simply an extended HashMap.
	 * It's used to store the MST and some expanded add and remove edge methods
	 * to make sure the edges are added in both directions.
	 */
	
	HashMap<String, LinkedList<String>> graph;
	LinkedList<Edge> MST = new LinkedList<Edge>();
	
	public Graph() {
		graph = new HashMap<String, LinkedList<String>>();
	}
	
	public void addMSTEdge(Edge e) {
		MST.add(e);
	}
	
	public int getMST() {
		int sum = 0;
		for (Edge e : MST) {
			sum += e.getDistance();
		}
		return sum;
	}
	
	public String MSTToString() {
		return MST.toString();
	}
	
	public boolean isCyclic(Edge e) {
		
		HashMap<String, Boolean> visited = new HashMap<>();

		// Add the potential connection
		this.addEdge(e);
		
		// Test if cyclic
		int layer = 1;
		if (isCyclicHelper(this, visited, e.getFrom(), e.getFrom(), layer)) {
			this.removeEdge(e);
			return true;
		} else {
			// keep the added edge
			return false;
		}
		
	}
	
	public boolean isCyclicHelper(HashMap<String, LinkedList<String>> graph, HashMap<String, Boolean> visited, String from, String parent, int layer) {
		
		/*
		 * Recursive function to detect loops. The condition for a loop is
		 * when a node expands to an already visited node that is not the current nodes parent
		 */
		visited.put(from, true);
		
		for (String s : graph.get(from)) {
			if (visited.get(s) == null) {
				isCyclicHelper(graph, visited, s, from, ++layer);
			} else if (!parent.equals(s) && visited.get(s) == true){
				return true;
			}
		}
		
		return false;
	}
	
	public void removeEdge(Edge e) {
		
		/*
		 * This method removes any edge from the graph. The method removes in both directions.
		 * It finds A and the mapped B. Likewise, it finds B and removes the mapped A.
		 */
		
		if (this.containsKey(e.getFrom()) && this.get(e.getFrom()).contains(e.getTo()) && this.containsKey(e.getTo()) && this.get(e.getTo()).contains(e.getFrom())) { // Graph contains both the key and the value 

			if (this.get(e.getFrom()).size() > 1) {
				this.get(e.getFrom()).remove(e.getTo());
			} else {
				this.remove(e.getFrom());
			}
			
			if (this.get(e.getTo()).size() > 1) {
				this.get(e.getTo()).remove(e.getFrom());
			} else {
				this.remove(e.getTo());
			}
		} else {
			throw new RuntimeException("Error while trying to remove the edge:\n " + e.toString() + "\n in:\n " + graph.get(e.getTo().toString()) + "\n and \n" + graph.get(e.getFrom().toString()));
		}
	}
	
	public void addEdge(Edge e) {
		
		/*
		 * This method adds an edge "in both directions". Once A has been mapped to B,
		 * it also maps B to A such that the graph doesn't become directed, but rather
		 * undirected in the sense that every edge is a two way street.
		 */
		
		// FROM --> TO
		if (!this.containsKey(e.getFrom()) ||this.get(e.getFrom()).isEmpty()) { // Values of the key not yet populated
			
			LinkedList<String> l = new LinkedList<>();
			l.add(e.getTo());
			this.put(e.getFrom(), l);
			
		} else if (!this.get(e.getFrom()).contains(e.getTo())) { // Values of the key doesn't already contain e.getTo()
			
			LinkedList<String> l = this.get(e.getFrom());
			l.add(e.getTo());
			this.put(e.getFrom(), l);
			
		}
		
		// TO --> FROM
		if (!this.containsKey(e.getTo()) || this.get(e.getTo()).isEmpty()) {
			
			LinkedList<String> l = new LinkedList<>();
			l.add(e.getFrom());
			this.put(e.getTo(), l);
			
		} else if (!this.get(e.getTo()).contains(e.getFrom())) {
			
			LinkedList<String> l = this.get(e.getTo());
			l.add(e.getFrom());
			this.put(e.getTo(), l);
			
		}
		
	}
	
}

class Edge implements Comparable<Edge> {
	
	/*
	 * This class represents an edge between two nodes (cities).
	 * 
	 * It implements the comparable interface in order to store it in the PriorityQueue.
	 * 
	 * We added a second constructor that takes a string array in order to easily create a new
	 * edge after using String.split() when extracting the two cities from the input
	 */
	
	private String from;
	private String to;
	private int distance;
	
	public Edge(String from, String to, int distance) {
		this.from = from;
		this.to = to;
		this.distance = distance;
	}
	
	public Edge(String[] fromTo, int distance) {
		if (fromTo.length != 2) throw new RuntimeException("Array too big for Edge creation! Thrown at: " + Arrays.toString(fromTo));
		
		this.from = fromTo[0];
		this.to = fromTo[1];
		this.distance = distance;
	}
	
	public String getFrom() { return this.from; }
	
	public String getTo() { return this.to; }
	
	public int getDistance() { return this.distance; }
	
	public String toString() {
		return from + "--" + to + " [" + distance + "]";
	}
	
	public int compareTo(Edge e) {
		if (this.distance > e.distance) {
			return 1;
		} else if (this.distance < e.distance) {
			return -1;
		} else {
			return 0;
		}
	}
}
