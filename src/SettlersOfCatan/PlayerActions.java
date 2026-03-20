package SettlersOfCatan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates all placement operations and board-query helpers for Settlers of Catan.
 * Satisfies the GRASP principles of High Cohesion and Low Coupling:
 * all board-interaction and placement logic is concentrated here, keeping
 * {@link Game} free of placement details and avoiding tight dependencies
 * between the game-loop and the board/bank/validator subsystems.
 * <p>
 * This class is fully functional for AI players. {@link HumanPlayerActions}
 * extends it to add interactive console handling.
 *
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class PlayerActions {

	/** The game board containing tiles, nodes, and edges */
	protected final Board board;

	/** Bank object for handling resource transactions */
	protected final Bank bank;

	/** Validator for checking placement rules */
	protected final IPlacementValidator validator;

	/** Array of all players in the game */
	protected final Player[] players;

	/** Random number generator for AI player decisions */
	protected final Random random;

	/** Total number of nodes on the Catan board */
	protected static final int TOTAL_NODES = 54;

	/** Maximum resources a player can have before being forced to build */
	protected static final int MAX_RESOURCES_BEFORE_BUILD = 7;

	/**
	 * Constructs a PlayerActions instance with all required collaborators.
	 * @param board The game board
	 * @param bank The bank for handling payments
	 * @param validator The placement validator
	 * @param players Array of all players (shared reference — changes visible here)
	 * @param random Random number generator for AI decisions
	 */
	public PlayerActions(Board board, Bank bank, IPlacementValidator validator,
			Player[] players, Random random) {
		this.board = board;
		this.bank = bank;
		this.validator = validator;
		this.players = players;
		this.random = random;
	}


	/**
	 * Orchestrates settlement placement during setup (no resource cost).
	 * GRASP: Controller — PlayerActions orchestrates the full placement flow.
	 * @param node The node where the settlement will be placed
	 * @param player The player placing the settlement
	 * @return true if placement succeeded, false otherwise
	 */
	public boolean placeSettlementSetup(Node node, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceSettlement(node, player, true)) {
			return false;
		}

		// 2. Use settlement piece (no resources paid during setup)
		if (!bank.useSettlementPieceSetup(player)) {
			return false;
		}

		// 3. Update node state
		node.setBuilding(new Settlement(player));
		node.setOccupyingPlayer(player);

		// 4. Add victory point
		player.addVictoryPoint(1);

		return true;
	}

	/**
	 * Orchestrates settlement placement during normal play.
	 * GRASP: Controller — PlayerActions orchestrates the full placement flow.
	 * @param node The node where the settlement will be placed
	 * @param player The player placing the settlement
	 * @return true if placement succeeded, false otherwise
	 */
	public boolean placeSettlement(Node node, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceSettlement(node, player, false)) {
			return false;
		}

		// Check road connectivity (normal play requirement)
		boolean hasRoadConnection = false;
		for (Edge edge : board.getEdges()) {
			if (edge != null && edge.touches(node) && edge.getRoad() != null
					&& edge.getRoad().getOwner() == player) {
				hasRoadConnection = true;
				break;
			}
		}
		if (!hasRoadConnection) {
			return false;
		}

		// 2. Pay for settlement
		if (!bank.payForSettlement(player)) {
			return false;
		}

		// 3. Update node state
		node.setBuilding(new Settlement(player));
		node.setOccupyingPlayer(player);

		return true;
	}

	/**
	 * Orchestrates city placement (upgrade settlement).
	 * GRASP: Controller — PlayerActions orchestrates the full placement flow.
	 * @param node The node where the city will be placed (must have a settlement)
	 * @param player The player placing the city
	 * @return true if placement succeeded, false otherwise
	 */
	public boolean placeCity(Node node, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceCity(node, player)) {
			return false;
		}

		// 2. Pay for city
		if (!bank.payForCity(player)) {
			return false;
		}

		// 3. Update node state (upgrade settlement to city)
		node.setBuilding(new City(player));
		node.setOccupyingPlayer(player);

		return true;
	}

	/**
	 * Orchestrates road placement during setup (no resource cost).
	 * GRASP: Controller — PlayerActions orchestrates the full placement flow.
	 * @param edge The edge where the road will be placed
	 * @param player The player placing the road
	 * @return true if placement succeeded, false otherwise
	 */
	public boolean placeRoadSetup(Edge edge, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceRoad(edge, player, true)) {
			return false;
		}

		// 2. Use road piece (no resources paid during setup)
		if (!bank.useRoadPieceSetup(player)) {
			return false;
		}

		// 3. Update edge state
		edge.setRoad(new Road(player, edge));

		return true;
	}

	/**
	 * Orchestrates road placement during normal play.
	 * GRASP: Controller — PlayerActions orchestrates the full placement flow.
	 * @param edge The edge where the road will be placed
	 * @param player The player placing the road
	 * @return true if placement succeeded, false otherwise
	 */
	public boolean placeRoad(Edge edge, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceRoad(edge, player, false)) {
			return false;
		}

		// Check connectivity (must be adjacent to player's building or road)
		Node nodeA = edge.getNodeA();
		Node nodeB = edge.getNodeB();
		boolean adjacentToBuilding = (nodeA.isOccupied() && nodeA.getOccupyingPlayer() == player)
				|| (nodeB.isOccupied() && nodeB.getOccupyingPlayer() == player);

		boolean adjacentToRoad = false;
		for (Edge otherEdge : board.getEdges()) {
			if (otherEdge != null && otherEdge != edge && otherEdge.getRoad() != null
					&& otherEdge.getRoad().getOwner() == player) {
				if (edge.touches(otherEdge.getNodeA()) || edge.touches(otherEdge.getNodeB())) {
					adjacentToRoad = true;
					break;
				}
			}
		}

		if (!adjacentToBuilding && !adjacentToRoad) {
			return false;
		}

		// 2. Pay for road
		if (!bank.payForRoad(player)) {
			return false;
		}

		// 3. Update edge state
		edge.setRoad(new Road(player, edge));

		return true;
	}


	/**
	 * Gets available actions for a player based on their resources and board state.
	 * @param player The player to check actions for
	 * @param mustBuild Whether the player is forced to build (7+ resources)
	 * @return List of available action strings
	 */
	public List<String> getAvailableActions(Player player, boolean mustBuild) {
		List<String> actions = new ArrayList<>();

		// Check if can build settlement
		if (player.canBuildSettlement() && !getAvailableSettlementNodesForPlayer(player).isEmpty()) {
			actions.add("SETTLEMENT");
		}

		// Check if can build city
		if (player.canBuildCity() && !getUpgradeableCityNodes(player).isEmpty()) {
			actions.add("CITY");
		}

		// Check if can build road
		if (player.canBuildRoad() && !getAvailableRoadEdgesForPlayer(player).isEmpty()) {
			actions.add("ROAD");
		}

		// Can pass only if not forced to build
		if (!mustBuild) {
			actions.add("PASS");
		}

		return actions;
	}

	/**
	 * Player builds a settlement (random choice from available locations).
	 * @param player The player building the settlement
	 * @param roundCount The current round number (for logging)
	 */
	public void buildSettlement(Player player, int roundCount) {
		List<Integer> availableNodes = getAvailableSettlementNodesForPlayer(player);

		if (availableNodes.isEmpty()) {
			System.out.println(player.getColor() + " - Cannot build settlement (no valid locations)");
			return;
		}

		// Choose random node from available options
		int nodeId = availableNodes.get(random.nextInt(availableNodes.size()));
		Node node = board.getNode(nodeId);

		if (node != null && placeSettlement(node, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Built settlement on node " + nodeId);
		} else {
			System.out.println(roundCount + " / " + player.getColor() + ": Failed to build settlement");
		}
	}

	/**
	 * Player builds a city (random choice from available locations).
	 * @param player The player building the city
	 * @param roundCount The current round number (for logging)
	 */
	public void buildCity(Player player, int roundCount) {
		List<Integer> upgradeableNodes = getUpgradeableCityNodes(player);

		if (upgradeableNodes.isEmpty()) {
			System.out.println(player.getColor() + " - Cannot build city (no settlements to upgrade)");
			return;
		}

		// Choose random settlement to upgrade
		int nodeId = upgradeableNodes.get(random.nextInt(upgradeableNodes.size()));
		Node node = board.getNode(nodeId);

		if (node != null && placeCity(node, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Built city on node " + nodeId);
		} else {
			System.out.println(roundCount + " / " + player.getColor() + ": Failed to build city");
		}
	}

	/**
	 * Player builds a road (random choice from available locations).
	 * @param player The player building the road
	 * @param roundCount The current round number (for logging)
	 */
	public void buildRoad(Player player, int roundCount) {
		List<Edge> availableEdges = getAvailableRoadEdgesForPlayer(player);

		if (availableEdges.isEmpty()) {
			System.out.println(player.getColor() + " - Cannot build road (no valid locations)");
			return;
		}

		// Choose random edge from available options
		Edge edge = availableEdges.get(random.nextInt(availableEdges.size()));

		if (placeRoad(edge, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Built road on edge " + edge.getId());
		} else {
			System.out.println(roundCount + " / " + player.getColor() + ": Failed to build road");
		}
	}

	/**
	 * Gets total resource count for a player.
	 * @param player The player to check
	 * @return The total number of resource cards the player has
	 */
	public int getTotalResourceCount(Player player) {
		return player.getTotalResourceCount();
	}

	/**
	 * Gets all available nodes for settlement placement (setup phase - distance rule only).
	 * @return List of node IDs where a settlement can be placed
	 */
	public List<Integer> getAvailableSettlementNodes() {
		List<Integer> available = new ArrayList<>();

		// Check all nodes on the board
		for (int i = 0; i < TOTAL_NODES; i++) {
			Node node = board.getNode(i);
			if (node != null && node.canPlaceBuilding()) {
				available.add(i);
			}
		}

		return available;
	}

	/**
	 * Gets available settlement nodes for a player during normal play (must be connected by road).
	 * @param player The player looking for settlement locations
	 * @return List of node IDs where the player can build a settlement
	 */
	public List<Integer> getAvailableSettlementNodesForPlayer(Player player) {
		List<Integer> available = new ArrayList<>();

		// Check all nodes on the board
		for (int i = 0; i < TOTAL_NODES; i++) {
			Node node = board.getNode(i);
			if (node != null && validator.canPlaceSettlement(node, player, false)
					&& player.canBuildSettlement()) {
				// Check if player has a road leading to this node
				boolean hasRoadConnection = false;
				for (Edge edge : board.getEdges()) {
					if (edge != null && edge.touches(node) && edge.getRoad() != null
							&& edge.getRoad().getOwner() == player) {
						hasRoadConnection = true;
						break;
					}
				}
				if (hasRoadConnection) {
					available.add(i);
				}
			}
		}

		return available;
	}

	/**
	 * Gets nodes where player can upgrade settlement to city.
	 * @param player The player looking for upgrade locations
	 * @return List of node IDs where the player can upgrade to a city
	 */
	public List<Integer> getUpgradeableCityNodes(Player player) {
		List<Integer> upgradeable = new ArrayList<>();

		// Check all nodes for settlements owned by this player
		for (int i = 0; i < TOTAL_NODES; i++) {
			Node node = board.getNode(i);
			if (node != null && validator.canPlaceCity(node, player) && player.canBuildCity()) {
				upgradeable.add(i);
			}
		}

		return upgradeable;
	}

	/**
	 * Gets available road edges for a player (connected to their roads/settlements).
	 * @param player The player looking for road locations
	 * @return List of edges where the player can build a road
	 */
	public List<Edge> getAvailableRoadEdgesForPlayer(Player player) {
		List<Edge> available = new ArrayList<>();

		// Get all edges that can be reached from player's buildings or roads
		for (Edge edge : board.getEdges()) {
			if (edge != null && edge.canPlaceRoad() && player.canBuildRoad()) {
				Node nodeA = edge.getNodeA();
				Node nodeB = edge.getNodeB();

				// Check if edge is adjacent to player's building
				boolean adjacentToBuilding =
						(nodeA.isOccupied() && nodeA.getOccupyingPlayer() == player)
						|| (nodeB.isOccupied() && nodeB.getOccupyingPlayer() == player);

				// Check if edge is adjacent to player's road
				boolean adjacentToRoad = false;
				for (Edge otherEdge : board.getEdges()) {
					if (otherEdge != null && otherEdge != edge && otherEdge.getRoad() != null
							&& otherEdge.getRoad().getOwner() == player) {
						if (edge.touches(otherEdge.getNodeA()) || edge.touches(otherEdge.getNodeB())) {
							adjacentToRoad = true;
							break;
						}
					}
				}

				if (adjacentToBuilding || adjacentToRoad) {
					available.add(edge);
				}
			}
		}

		return available;
	}

	/**
	 * Gets all node IDs that the player currently occupies (settlements/cities).
	 * @param player the player
	 * @return list of occupied node IDs
	 */
	public List<Integer> getOccupiedNodeIds(Player player) {
		List<Integer> occupied = new ArrayList<>();

		for (int i = 0; i < TOTAL_NODES; i++) {
			Node node = board.getNode(i);
			if (node != null && node.isOccupied() && node.getOccupyingPlayer() == player) {
				occupied.add(i);
			}
		}

		return occupied;
	}

	/**
	 * Gets all node IDs adjacent to the given node that are NOT occupied.
	 * @param nodeId the node to find adjacent unoccupied nodes for
	 * @return list of adjacent unoccupied node IDs
	 */
	public List<Integer> getAdjacentUnoccupiedNodeIds(int nodeId) {
		List<Integer> adjacent = new ArrayList<>();
		Node node = board.getNode(nodeId);
		if (node == null) {
			return adjacent;
		}

		// Check all edges to find adjacent nodes
		for (Edge edge : board.getEdges()) {
			if (edge != null && edge.touches(node)) {
				Node otherNode = (edge.getNodeA() == node) ? edge.getNodeB() : edge.getNodeA();

				// Only include if not occupied
				if (!otherNode.isOccupied()) {
					int otherId = otherNode.getId();
					if (!adjacent.contains(otherId)) {
						adjacent.add(otherId);
					}
				}
			}
		}

		return adjacent;
	}
}
