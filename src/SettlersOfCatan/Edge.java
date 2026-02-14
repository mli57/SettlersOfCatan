package SettlersOfCatan;

/**
 * An edge between two nodes A and B. Each edge can hold at most one Road.
 * Adjacency is defined by boolean logic: two edges are adjacent iff they share a node.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Edge {
	/** Unique ID for this edge **/
	private final int id;
	
	/** First endpoint node **/
	private final Node nodeA;
	
	/** Second endpoint node **/
	private final Node nodeB;
	
	/** Road placed on this edge, or null if empty **/
	private Road road;

	/**
	 * Constructor for a new edge.
	 * @param id The unique ID of the edge
	 * @param nodeA First endpoint node
	 * @param nodeB Second endpoint node
	 */
	public Edge(int id, Node nodeA, Node nodeB) {
		// Initialize edge with ID and endpoints
		this.id = id;
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}

	/**
	 * Gets the edge ID.
	 * @return The unique ID of the edge
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the first endpoint node.
	 * @return Node A
	 */
	public Node getNodeA() {
		return nodeA;
	}

	/**
	 * Gets the second endpoint node.
	 * @return Node B
	 */
	public Node getNodeB() {
		return nodeB;
	}

	/**
	 * Gets the road on this edge.
	 * @return The road, or null if empty
	 */
	public Road getRoad() {
		return road;
	}

	/**
	 * Sets the road on this edge. Called when a player builds a road here.
	 * @param road The road to place
	 */
	public void setRoad(Road road) {
		this.road = road;
	}

	/**
	 * Gets the player who has a road on this edge.
	 * @return The occupying player, or null if no road
	 */
	public Player getOccupyingPlayer() {
		if (road == null) {
			return null;
		}
		return road.getOwner();
	}

	/**
	 * Checks if a road can be placed on this edge.
	 * @return true if no road is currently on this edge
	 */
	public boolean canPlaceRoad() {
		return road == null;
	}

	/**
	 * Checks if this edge is adjacent to another edge (they share exactly one node).
	 * @param other The other edge to check
	 * @return true if edges are adjacent
	 */
	public boolean isAdjacentTo(Edge other) {
		return other != null
				&& this != other
				&& (touches(other.nodeA) || touches(other.nodeB));
	}

	/**
	 * Checks if this edge touches the given node.
	 * @param node The node to check
	 * @return true if the edge connects to this node
	 */
	public boolean touches(Node node) {
		return node != null && (node == nodeA || node == nodeB);
	}

}
