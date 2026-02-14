package SettlersOfCatan;

/**
 * Interface for validating placement rules.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public interface IPlacementValidator {
	/**
	 * Checks if a settlement can be placed at the given node.
	 * @param node the node to check
	 * @param player the player attempting placement
	 * @param isSetupPhase true if this is during initial setup
	 * @return true if placement is valid
	 */
	boolean canPlaceSettlement(Node node, Player player, boolean isSetupPhase);

	/**
	 * Checks if a road can be placed on the given edge.
	 * @param edge the edge to check
	 * @param player the player attempting placement
	 * @param isSetupPhase true if this is during initial setup
	 * @return true if placement is valid
	 */
	boolean canPlaceRoad(Edge edge, Player player, boolean isSetupPhase);

	/**
	 * Checks if a city can be placed (upgrade settlement) at the given node.
	 * @param node the node to check
	 * @param player the player attempting placement
	 * @return true if upgrade is valid
	 */
	boolean canPlaceCity(Node node, Player player);
}

