package SettlersOfCatan;

import java.util.List;
import java.util.ArrayList;

/**
 * A vertex between tiles on the board where a settlement/city can be built.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Node {

	/** Unique id for the node on the board */
	private final int id;

	/** The type of building currently occupying the node */
	private Building building;

	/** The player who owns the building at the node  */
	private Player occupyingPlayer;

	/** List of nodes directly connected to the node via edges  */
	private List<Node> adjacentNodes;

	/** List of tiles that touch the node */
	private List<Tile> adjacentTiles;


	/**
	 * Constructor for a new node.
	 * @param id The unique ID of the node (0-53)
	 */
	public Node(int id){
		// Initialize node with ID
		this.id = id;
		this.building = null;
		this.occupyingPlayer = null;
		
		// Initialize adjacency lists
		this.adjacentNodes = new ArrayList<>();
		this.adjacentTiles = new ArrayList<>();
	}


	// GETTERS & SETTERS

	/**
	 * Gets the node ID.
	 * @return The unique ID of the node
	 */
	public int getId(){
		return id;
	}

	/**
	 * Gets the current building on this node.
	 * @return The building, or null if empty
	 */
	public Building getBuilding(){
		return building;
	}

	/**
	 * Sets the building on this node.
	 * @param building The building to place
	 */
	public void setBuilding(Building building){
		this.building = building;
	}

	/**
	 * Gets the player occupying this node.
	 * @return The occupying player, or null if empty
	 */
	public Player getOccupyingPlayer(){
		return occupyingPlayer;
	}

	/**
	 * Sets the player occupying this node.
	 * @param player The player to set as occupant
	 */
	public void setOccupyingPlayer(Player player){
		this.occupyingPlayer = player;
	}

	/**
	 * Gets the list of adjacent nodes.
	 * @return List of nodes connected via edges
	 */
	public List<Node> getAdjacentNodes() {
		return adjacentNodes;
	}

	/**
	 * Gets the list of adjacent tiles.
	 * @return List of tiles that touch this node
	 */
	public List<Tile> getAdjacentTiles() {
		return adjacentTiles;
	}



	// GAME LOGIC METHODS

	/**
	 * Adds an adjacent node to this node's adjacency list.
	 * @param node The adjacent node to add
	 */
	public void addAdjacentNode(Node node){
		if(node != null && !adjacentNodes.contains(node)){
			adjacentNodes.add(node);
		}
	}

	/**
	 * Adds an adjacent tile to this node's tile list.
	 * @param tile The adjacent tile to add
	 */
	public void addAdjacentTile(Tile tile){
		if(tile != null && !adjacentTiles.contains(tile)){
			adjacentTiles.add(tile);
		}
	}

	/**
	 * Checks if a building can be placed at this node (distance rule).
	 * @return true if building can be placed, false otherwise
	 */
	public boolean canPlaceBuilding() {
		// Check if node already has a building
		if (building != null){
			return false;
		}

		// Check distance rule: no adjacent nodes can have buildings
		for (Node adjacent : adjacentNodes) {
			if (adjacent.getBuilding() != null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if the node is occupied by any player.
	 * @return true if the node has a building, false otherwise
	 */
	public boolean isOccupied(){
		return building != null;
	}


}
