package SettlersOfCatan;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a player in the Settlers of Catan game.
 * Manages player resources, buildings, and victory points.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Player {
	/** The player's color (RED, BLUE, WHITE, ORANGE) **/
	private PlayerColor color;
	
	/** Map of resource types to quantities owned by this player **/
	private Map<ResourceType, Integer> resources;
	
	/** Map of building types to remaining pieces available **/
	private Map<BuildingType, Integer> buildings;
	
	/** Current victory points earned by this player **/
	private int victoryPoints;

	/**
	 * Constructor for a new player.
	 * @param color The player's assigned color
	 */
	public Player(PlayerColor color){
		// Initialize player attributes
		this.color = color;
		this.victoryPoints = 0;
		
		// Initialize resource and building maps
		resources = new EnumMap<ResourceType, Integer>(ResourceType.class);
		buildings = new EnumMap<BuildingType, Integer>(BuildingType.class);
		
		// Set all resources to zero initially
		for (int i = 0; i<ResourceType.values().length; i++){
			resources.put(ResourceType.values()[i], 0);
		}
		
		// Initialize building counts (starting pieces)
		buildings.put(BuildingType.ROAD, 15);
		buildings.put(BuildingType.SETTLEMENT, 5);
		buildings.put(BuildingType.CITY, 4);
	}

	/**
	 * Adds one resource card of the specified type to the player's hand.
	 * @param res The type of resource to add
	 */
	public void addResource(ResourceType res) {
		// Ignore NULL resources
		if (res == ResourceType.NULL){
			return;
		}

		// Get current count and increment
		int current = resources.get(res);
		resources.put(res, current + 1);
	}

	/**
	 * Removes a specified amount of a resource type from the player's hand.
	 * @param res The type of resource to remove
	 * @param amount The quantity to remove
	 * @return true if player had enough resources and removal succeeded, false otherwise
	 */
	public boolean removeResource(ResourceType res, int amount) {
		// Get current resource count
		int current = resources.get(res);

		// Check if player has enough resources
		if ((current - amount) < 0){
			return false;
		}

		// Deduct resources
		resources.put(res, current - amount);
		return true;
	}

	/**
	 * Checks if player can afford a given cost map.
	 * @param cost map of resource types to required amounts
	 * @return true if player has enough of each resource
	 */
	public boolean canAfford(Map<ResourceType, Integer> cost) {

		for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
			if (resources.get(entry.getKey()) < entry.getValue()) {
				return false;
			}
		}

		return true;

	}

	/**
	 * Convenience method: checks if player can build a road.
	 * @return true if player can afford road cost and has road pieces
	 */
	public boolean canBuildRoad() {

		Map<ResourceType, Integer> roadCost = Bank.getRoadCost();
		return canAfford(roadCost) && buildings.get(BuildingType.ROAD) > 0;

	}

	/**
	 * Convenience method: checks if player can build a settlement.
	 * @return true if player can afford settlement cost and has settlement pieces
	 */
	public boolean canBuildSettlement() {

		Map<ResourceType, Integer> settlementCost = Bank.getSettlementCost();
		return canAfford(settlementCost) && buildings.get(BuildingType.SETTLEMENT) > 0;

	}

	/**
	 * Convenience method: checks if player can build a city.
	 * @return true if player can afford city cost and has city pieces
	 */
	public boolean canBuildCity() {

		Map<ResourceType, Integer> cityCost = Bank.getCityCost();
		return canAfford(cityCost) && buildings.get(BuildingType.CITY) > 0;

	}

	/**
	 * Uses one road piece without paying resources (for initial setup).
	 */
	public void useRoadPiece() {
		buildings.put(
			BuildingType.ROAD,
			buildings.get(BuildingType.ROAD) - 1
		);
	}

	/**
	 * Gets the player's current victory points.
	 * @return The number of victory points
	 */
	public int getVictoryPoints() {
		return this.victoryPoints;
	}

	/**
	 * Uses one settlement piece without paying resources (for initial setup).
	 */
	public void useSettlementPiece() {
		buildings.put(
			BuildingType.SETTLEMENT,
			buildings.get(BuildingType.SETTLEMENT) - 1
		);
	}

	/**
	 * Uses one city piece.
	 */
	public void useCityPiece() {
		buildings.put(
			BuildingType.CITY,
			buildings.get(BuildingType.CITY) - 1
		);
	}

	/**
	 * Adds victory points to the player.
	 * @param points The number of victory points to add
	 */
	public void addVictoryPoint(int points) {
		this.victoryPoints += points;
	}

	/**
	 * Gets the player's color
	 * @return the player's color
	 */
	public PlayerColor getColor() {
		return color;
	}

	/**
	 * Gets the buildings map (for checking available pieces).
	 * @return the buildings map
	 */
	public Map<BuildingType, Integer> getBuildings() {
		return buildings;
	}

	/**
	 * Gets the resources map.
	 * @return the resources map
	 */
	public Map<ResourceType, Integer> getResources() {
		return resources;
	}

	/**
	 * Gets total resource count.
	 * @return total number of resource cards
	 */
	public int getTotalResourceCount() {
		int total = 0;
		for (Integer count : resources.values()) {
			total += count;
		}
		return total;
	}
}
