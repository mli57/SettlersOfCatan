package SettlersOfCatan;

import java.util.Map;

/**
 * A hex tile on the board. Produces a resource when corresponding number is rolled (except Desert).
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Tile {
	/** Map from terrain types to the resource they produce **/
	private static final Map<TerrainType, ResourceType> TERRAIN_TO_RESOURCE = Map.of(
		TerrainType.FOREST, ResourceType.WOOD,
		TerrainType.PASTURE, ResourceType.SHEEP,
		TerrainType.FIELDS, ResourceType.WHEAT,
		TerrainType.HILLS, ResourceType.BRICK,
		TerrainType.MOUNTAINS, ResourceType.ORE,
		TerrainType.DESERT, ResourceType.NULL
	);
	
	/** Q coordinate in cube coordinate system **/
	private final int q;
	
	/** S coordinate in cube coordinate system **/
	private final int s;
	
	/** R coordinate in cube coordinate system **/
	private final int r;
	
	/** The terrain type of this tile **/
	private final TerrainType terrain;
	
	/** Dice number 2-12 (0 for desert) **/
	private final int number;
	
	/** Array of node IDs (0-53) that touch this tile **/
	private final int[] nodeIds;

	/**
	 * Constructor for a new tile.
	 * @param q The q coordinate
	 * @param s The s coordinate
	 * @param r The r coordinate
	 * @param terrain The terrain type
	 * @param number The dice number (2-12, or 0 for desert)
	 * @param nodeIds Array of 6 node IDs that touch this tile
	 */
	public Tile(int q, int s, int r, TerrainType terrain, int number, int[] nodeIds) {
		// Initialize tile coordinates
		this.q = q;
		this.s = s;
		this.r = r;
		
		// Initialize tile properties
		this.terrain = terrain;
		this.number = number;
		this.nodeIds = nodeIds;
	}

	/**
	 * Gets the q coordinate.
	 * @return The q coordinate
	 */
	public int getQ(){
		return q;
	}

	/**
	 * Gets the s coordinate.
	 * @return The s coordinate
	 */
	public int getS(){
		return s;
	}

	/**
	 * Gets the r coordinate.
	 * @return The r coordinate
	 */
	public int getR(){
		return r;
	}

	/**
	 * Gets the terrain type.
	 * @return The terrain type
	 */
	public TerrainType getTerrain(){
		return terrain;
	}

	/**
	 * Gets the dice number.
	 * @return The dice number (2-12, or 0 for desert)
	 */
	public int getNumber(){
		return number;
	}
	/**
	 * Returns the array of node IDs (0-53) that touch this tile.
	 * To get the actual Node objects, use Board.getNode(id) for each ID.
	 * Returns a defensive copy to prevent mutation.
	 * @return A defensive copy of the node IDs array, or null if nodeIds is null
	 */
	public int[] getNodeIds(){
		if (nodeIds == null) {
			return null;
		}
		// Create defensive copy
		int[] copy = new int[nodeIds.length];
		for (int i = 0; i < nodeIds.length; i++) {
			copy[i] = nodeIds[i];
		}
		return copy;
	}

	/**
	 * Produces resource for adjacent settlements/cities when this number is rolled.
	 * Desert produces nothing.
	 * @return The resource type produced, or NULL for desert
	 */
	public ResourceType produceResource(){
		// Look up resource type from terrain
		return TERRAIN_TO_RESOURCE.getOrDefault(terrain, ResourceType.NULL);
	}
}
