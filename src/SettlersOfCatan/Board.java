package SettlersOfCatan;

/**
 * Data holder for tiles, nodes, and edges with lookup methods.
 * SOLID: Single Responsibility - only stores and retrieves board data.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Board {
	/** Array of all hex tiles on the board **/
	private Tile[] tiles;
	
	/** Array of all intersection nodes on the board **/
	private Node[] nodes;
	
	/** Array of all edges (roads) connecting nodes **/
	private Edge[] edges;

	/**
	 * Sets the tiles array (called by board generator).
	 * @param tiles The array of tiles to set
	 */
	public void setTiles(Tile[] tiles) {
		this.tiles = tiles;
	}

	/**
	 * Sets the nodes array (called by board generator).
	 * @param nodes The array of nodes to set
	 */
	public void setNodes(Node[] nodes) {
		this.nodes = nodes;
	}

	/**
	 * Sets the edges array (called by board generator).
	 * @param edges The array of edges to set
	 */
	public void setEdges(Edge[] edges) {
		this.edges = edges;
	}

	/**
	 * Gets all nodes on the board.
	 * @return a defensive copy of the nodes array
	 */
	public Node[] getNodes() {
		return nodes == null ? null : nodes.clone();
	}

	/**
	 * Gets a tile by its cube coordinates.
	 * @param q The q coordinate
	 * @param s The s coordinate
	 * @param r The r coordinate
	 * @return The tile at these coordinates, or null if not found
	 */
	public Tile getTile(int q, int s, int r) {
		// Check if tiles are initialized
		if (tiles == null) {
			return null;
		}
		
		// Search for tile with matching coordinates
		for (Tile tile : tiles){
			if (tile.getQ() == q && tile.getS() == s && tile.getR() == r){
				return tile;
			}
		}
		
		// Tile not found
		return null;
	}

	/**
	 * Gets a node by its ID.
	 * @param id The node ID (0-53)
	 * @return The node with this ID, or null if invalid
	 */
	public Node getNode(int id) {
		if (nodes == null || id < 0 || id >= nodes.length) {
			return null;
		}
		return nodes[id];
	}

	/**
	 * Gets an edge by its ID.
	 * @param id the edge ID
	 * @return the edge, or null if not found
	 */
	public Edge getEdge(int id) {

		if (edges == null || id < 0 || id >= edges.length) {
			return null;
		}

		return edges[id];

	}

	/**
	 * Finds an edge between two nodes (checks both node orders).
	 * @param nodeIdA first node ID
	 * @param nodeIdB second node ID
	 * @return the edge if found, null otherwise
	 */
	public Edge findEdge(int nodeIdA, int nodeIdB) {
		// Check if edges are initialized
		if (edges == null) {
			return null;
		}
		
		// Search through all edges
		for (Edge edge : edges) {
			if (edge != null) {
				int edgeNodeA = edge.getNodeA().getId();
				int edgeNodeB = edge.getNodeB().getId();
				// Check both orders: (A,B) or (B,A)
				if ((edgeNodeA == nodeIdA && edgeNodeB == nodeIdB) ||
					(edgeNodeA == nodeIdB && edgeNodeB == nodeIdA)) {
					return edge;
				}
			}
		}
		// Edge not found
		return null;
	}


	/**
	 * Gets all tiles on the board
	 * @return a defensive copy of the tiles array
	 */
	public Tile[] getTiles() {
		return tiles == null ? null : tiles.clone();
	}

	/**
	 * Gets all edges on the board
	 * @return a defensive copy of the edges array
	 */
	public Edge[] getEdges() {
		return edges == null ? null : edges.clone();
	}
}
