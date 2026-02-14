package SettlersOfCatan;

/**
 * Validates placement rules for buildings and roads.
 * SOLID: Single Responsibility - only handles rule validation.
 */
public class PlacementValidator implements IPlacementValidator {

	@Override
	public boolean canPlaceSettlement(Node node, Player player, boolean isSetupPhase) {

		// Validate input parameters
		if (node == null) {
			return false;
		}
		
		// During setup, player can be null (we only check distance rule)
		if (isSetupPhase && player == null) {
			return node.canPlaceBuilding();
		}
		
		if (player == null) {
			return false;
		}

		// Check distance rule (node must be empty and no adjacent buildings)
		if (!node.canPlaceBuilding()) {
			return false;
		}

		// Distance rule check passed

		// During setup, no road connection needed
		if (isSetupPhase) {
			return true;
		}

		// During normal play, player must have a road leading to this node
		// This check would need Board reference - for now, we'll handle it in Game
		// since we need to check edges
		return true; // Game will check road connectivity

	}

	@Override
	public boolean canPlaceRoad(Edge edge, Player player, boolean isSetupPhase) {

		// Validate input and edge availability
		if (edge == null || player == null || !edge.canPlaceRoad()) {
			return false;
		}

		// During setup, road must touch a settlement just placed
		if (isSetupPhase) {
			Node nodeA = edge.getNodeA();
			Node nodeB = edge.getNodeB();

			// Check if either endpoint has player's settlement
			return (nodeA.isOccupied() && nodeA.getOccupyingPlayer() == player) ||
					(nodeB.isOccupied() && nodeB.getOccupyingPlayer() == player);
		}

		// During normal play, must be connected to player's road network or building
		// This check would need Board reference - Game will handle it
		return true; // Game will check connectivity

	}

	@Override
	public boolean canPlaceCity(Node node, Player player) {

		// Validate input parameters
		if (node == null || player == null) {
			return false;
		}

		// Must already have a settlement owned by this player
		if (!(node.getBuilding() instanceof Settlement) || node.getOccupyingPlayer() != player) {
			return false;
		}

		// City upgrade is valid
		return true;

	}
}

