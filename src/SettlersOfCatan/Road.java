package SettlersOfCatan;

/**
 * A road placed on an edge and owned by a player. Used for longest road calculation.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Road {
	/** The player who owns this road **/
	private final Player owner;
	
	/** The edge this road is placed on **/
	private final Edge edge;

	/**
	 * Constructor for a road.
	 * @param owner The player who owns this road
	 * @param edge The edge this road is placed on
	 */
	public Road(Player owner, Edge edge) {
		this.owner = owner;
		this.edge = edge;
	}

	/**
	 * Gets the owner of this road.
	 * @return The player who owns this road
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Gets the edge this road is placed on.
	 * @return The edge
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * Gets the length of longest continuous road through this road.
	 * Not implemented yet - returns 0.
	 * @return 0 (not implemented)
	 */
	public int getLongestRoadCount() {
		return 0;
	}
}
