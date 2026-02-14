package SettlersOfCatan;

import java.util.EnumMap;
import java.util.Map;

/**
 * Handles all resource transactions and building costs.
 * SOLID: Single Responsibility - manages all payment logic.
 * GRASP: Pure Fabrication - coordinates between Player and building costs.
 */
public class Bank {
	// Building costs as static final Maps
	private static final Map<ResourceType, Integer> SETTLEMENT_COST = new EnumMap<>(ResourceType.class);
	private static final Map<ResourceType, Integer> CITY_COST = new EnumMap<>(ResourceType.class);
	private static final Map<ResourceType, Integer> ROAD_COST = new EnumMap<>(ResourceType.class);

	static {
		// Initialize settlement costs
		SETTLEMENT_COST.put(ResourceType.WOOD, 1);
		SETTLEMENT_COST.put(ResourceType.BRICK, 1);
		SETTLEMENT_COST.put(ResourceType.SHEEP, 1);
		SETTLEMENT_COST.put(ResourceType.WHEAT, 1);

		// Initialize city costs
		CITY_COST.put(ResourceType.ORE, 3);
		CITY_COST.put(ResourceType.WHEAT, 2);

		// Initialize road costs
		ROAD_COST.put(ResourceType.WOOD, 1);
		ROAD_COST.put(ResourceType.BRICK, 1);
		// All costs initialized
	}

	/**
	 * Attempts to pay for a settlement.
	 * @param player the player paying
	 * @return true if payment succeeded
	 */
	public boolean payForSettlement(Player player) {

		// Check if player can afford settlement
		if (!player.canAfford(SETTLEMENT_COST) || player.getBuildings().get(BuildingType.SETTLEMENT) <= 0) {
			return false;
		}

		// Deduct resources from player
		for (Map.Entry<ResourceType, Integer> entry : SETTLEMENT_COST.entrySet()) {
			player.removeResource(entry.getKey(), entry.getValue());
		}

		// Consume settlement piece from inventory
		player.useSettlementPiece();

		// Add victory point for new settlement
		player.addVictoryPoint(1);

		// Payment successful
		return true;

	}

	/**
	 * Attempts to pay for a city upgrade.
	 * @param player the player paying
	 * @return true if payment succeeded
	 */
	public boolean payForCity(Player player) {

		if (!player.canAfford(CITY_COST) || player.getBuildings().get(BuildingType.CITY) <= 0) {
			return false;
		}

		// Deduct resources
		for (Map.Entry<ResourceType, Integer> entry : CITY_COST.entrySet()) {
			player.removeResource(entry.getKey(), entry.getValue());
		}

		// Consume city piece
		player.useCityPiece();

		// Net +1 victory point (settlement 1 -> city 2)
		player.addVictoryPoint(1);

		return true;

	}

	/**
	 * Attempts to pay for a road.
	 * @param player the player paying
	 * @return true if payment succeeded
	 */
	public boolean payForRoad(Player player) {

		if (!player.canAfford(ROAD_COST) || player.getBuildings().get(BuildingType.ROAD) <= 0) {
			return false;
		}

		// Deduct resources
		for (Map.Entry<ResourceType, Integer> entry : ROAD_COST.entrySet()) {
			player.removeResource(entry.getKey(), entry.getValue());
		}

		// Consume road piece
		player.useRoadPiece();

		return true;

	}

	/**
	 * Uses a road piece during setup (no resource cost).
	 * @param player the player
	 * @return true if player has road pieces available
	 */
	public boolean useRoadPieceSetup(Player player) {
		if (player.getBuildings().get(BuildingType.ROAD) <= 0) {
			return false;
		}
		player.useRoadPiece();
		return true;
	}

	/**
	 * Uses a settlement piece during setup (no resource cost).
	 * @param player the player
	 * @return true if player has settlement pieces available
	 */
	public boolean useSettlementPieceSetup(Player player) {
		if (player.getBuildings().get(BuildingType.SETTLEMENT) <= 0) {
			return false;
		}
		player.useSettlementPiece();
		return true;
	}

	/**
	 * Gets the cost for a settlement.
	 * @return unmodifiable map of resource costs
	 */
	public static Map<ResourceType, Integer> getSettlementCost() {
		return new EnumMap<>(SETTLEMENT_COST);
	}

	/**
	 * Gets the cost for a city.
	 * @return unmodifiable map of resource costs
	 */
	public static Map<ResourceType, Integer> getCityCost() {
		return new EnumMap<>(CITY_COST);
	}

	/**
	 * Gets the cost for a road.
	 * @return unmodifiable map of resource costs
	 */
	public static Map<ResourceType, Integer> getRoadCost() {
		return new EnumMap<>(ROAD_COST);
	}
}

