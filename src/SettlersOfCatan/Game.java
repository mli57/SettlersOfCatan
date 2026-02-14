package SettlersOfCatan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main game controller that orchestrates the Settlers of Catan game flow.
 * Handles setup phase, game loop, resource distribution, and player actions.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Game {
	/** The game board containing tiles, nodes, and edges **/
	private Board board;

	/**
	 * Gets the game board.
	 * @return the board
	 */
	public Board getBoard() {
		return board;
	}
	
	/** Array of all players in the game **/
	private Player[] players;
	
	/** Index of the current player whose turn it is **/
	private int currentPlayer;
	
	/** Current round number (starts at 0) **/
	private int roundCount;

	/** Dice object for rolling dice **/
	private Dice dice;

	/** Bank object for handling resource transactions **/
	private Bank bank;

	/** Validator for checking placement rules **/
	private IPlacementValidator validator;

	/** Random number generator for AI player decisions **/
	private Random random;

	/** Victory points needed to win the game **/
	private static final int VICTORY_POINTS_TO_WIN = 10;
	
	/** Maximum resources a player can have before being forced to build **/
	private static final int MAX_RESOURCES_BEFORE_BUILD = 7;
	
	/** Number of sides on each die **/
	private static final int DICE_SIDES = 6;
	
	/** Total number of nodes on the Catan board **/
	private static final int TOTAL_NODES = 54;

	/**
	 * Constructor with dependency injection.
	 * @param board The game board
	 * @param dice The dice object for rolling
	 * @param bank The bank for handling payments
	 * @param validator The placement validator
	 * @param numPlayers The number of players in the game
	 */
	public Game(Board board, Dice dice, Bank bank, IPlacementValidator validator, int numPlayers) {
		this.board = board;
		this.dice = dice;
		this.bank = bank;
		this.validator = validator;
		this.players = new Player[numPlayers];
		this.random = new Random();
		this.currentPlayer = 0;
		this.roundCount = 0;
		
		// Initialize players
		initializePlayers();
	}

	/**
	 * Initializes all players with their assigned colors.
	 */
	public void initializePlayers(){
		// Initialize all players with their colors
		for (int i = 0; i < players.length; i++){
			players[i] = new Player(PlayerColor.values()[i]);
		}
	}

	/**
	 * Setup phase: each player places 2 settlements and 2 roads in one go.
	 */
	public void setupInitialSettlements() {
		// Each player takes their turn to place initial buildings
		for (int i = 0; i < players.length; i++) {
			Player player = players[i]; // Current player setting up
			System.out.println("\n=== " + player.getColor() + " Player Setup ===");

			// Place first settlement
			placeSettlement(player, i, 1);
			
			// Place first road
			placeRoad(player, i, 1);
			
			// Place second settlement
			placeSettlement(player, i, 2);
			
			// Place second road
			placeRoad(player, i, 2);
		}
	}

	/**
	 * Places one settlement for a player during setup (random choice).
	 * @param player The player placing the settlement
	 * @param playerIndex The index of the player in the players array
	 * @param settlementNumber Which settlement this is (1 or 2)
	 */
	private void placeSettlement(Player player, int playerIndex, int settlementNumber) {
		// Get all available nodes (following distance rule)
		List<Integer> availableNodes = getAvailableSettlementNodes();
		
		if (availableNodes.isEmpty()) {
			System.out.println(player.getColor() + " - No available nodes for settlement #" + settlementNumber);
			return;
		}
		
		// Choose randomly from available nodes
		int nodeId = availableNodes.get(random.nextInt(availableNodes.size()));
		Node node = board.getNode(nodeId);
		
		if (node != null && placeSettlementSetup(node, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Placed settlement #" + settlementNumber + " on node " + nodeId);
		}
	}

	/**
	 * Places one road for a player during setup (random choice).
	 * @param player The player placing the road
	 * @param playerIndex The index of the player in the players array
	 * @param roadNumber Which road this is (1 or 2)
	 */
	private void placeRoad(Player player, int playerIndex, int roadNumber) {
		// Get all nodes the player occupies
		List<Integer> occupiedNodes = getOccupiedNodeIds(player);
		
		if (occupiedNodes.isEmpty()) {
			System.out.println(player.getColor() + " - No settlements to build roads from");
			return;
		}
		
		// Choose random starting node from player's settlements
		int firstNodeId = occupiedNodes.get(random.nextInt(occupiedNodes.size()));
		
		// Get adjacent unoccupied nodes
		List<Integer> adjacentUnoccupied = getAdjacentUnoccupiedNodeIds(firstNodeId);
		
		if (adjacentUnoccupied.isEmpty()) {
			// Try another occupied node if first one has no adjacent unoccupied nodes
			for (int occupiedId : occupiedNodes) {
				if (occupiedId != firstNodeId) {
					adjacentUnoccupied = getAdjacentUnoccupiedNodeIds(occupiedId);
					if (!adjacentUnoccupied.isEmpty()) {
						firstNodeId = occupiedId;
						break;
					}
				}
			}
		}
		
		if (adjacentUnoccupied.isEmpty()) {
			System.out.println(player.getColor() + " - No available adjacent nodes for road #" + roadNumber);
			return;
		}
		
		// Choose random destination node
		int secondNodeId = adjacentUnoccupied.get(random.nextInt(adjacentUnoccupied.size()));
		
		// Find the edge between the two nodes
		Edge edge = board.findEdge(firstNodeId, secondNodeId);
		if (edge != null && placeRoadSetup(edge, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Placed road #" + roadNumber + " from node " + firstNodeId + " to node " + secondNodeId);
		}
	}

	/**
	 * Starts the game with no round limit. First sets up initial settlements, then runs the game loop.
	 * Game continues until a player reaches the victory point goal.
	 */
	public void startGame() {
		startGame(Integer.MAX_VALUE);
	}

	/**
	 * Starts the game with a maximum round limit.
	 * @param maxRounds maximum number of rounds to play
	 */
	public void startGame(int maxRounds) {
		setupInitialSettlements();
		
		System.out.println("\n=== GAME START ===");
		
		// Game loop - continue until someone wins or max rounds reached
		while (getWinner() == null) {
			// Check if we've reached max rounds before starting this round
			if (roundCount >= maxRounds) {
				System.out.println("\n=== GAME OVER ===");
				System.out.println("Maximum rounds (" + maxRounds + ") reached!");
				// Find player with most victory points
				Player winner = players[0];
				for (int i = 1; i < players.length; i++) {
					if (players[i].getVictoryPoints() > winner.getVictoryPoints()) {
						winner = players[i];
					}
				}
				System.out.println(winner.getColor() + " Player wins with " + winner.getVictoryPoints() + " victory points!");
				return;
			}
			
			// Process each player's turn in this round
			for (int i = 0; i < players.length; i++) {
				currentPlayer = i;
				Player player = players[i];
				
				// Check for winner before each turn
				if (getWinner() != null) {
					break;
				}
				
				System.out.println("\n--- " + player.getColor() + " Player's Turn ---");
				
				// Roll dice for this turn
				int diceRoll = dice.rollTwoDice(DICE_SIDES);
				System.out.println("Dice roll: " + diceRoll);
				
		// Distribute resources to all players
		distributeResources(diceRoll);
		
		// Player actions - build or pass
		playerTurn(player, i);
		
		// End of turn processing
			}
			
			// Print victory points at end of round (R1.7 requirement)
			System.out.println("\n=== End of Round " + roundCount + " - Victory Points ===");
			for (int i = 0; i < players.length; i++) {
				System.out.println(players[i].getColor() + " Player: " + players[i].getVictoryPoints() + " VP");
			}
			System.out.println();
			
			// Increment round counter after all players have taken their turn
			roundCount++;
		}
		
		// Game over - determine winner
		Player winner = getWinner();
		if (winner != null) {
			System.out.println("\n=== GAME OVER ===");
			System.out.println(winner.getColor() + " Player wins with " + winner.getVictoryPoints() + " victory points!");
		}
	}

	/**
	 * Handles a player's turn - they choose randomly from available actions.
	 * @param player The player whose turn it is
	 * @param playerIndex The index of the player in the players array
	 */
	private void playerTurn(Player player, int playerIndex) {
		// Count total resources
		int totalResources = getTotalResourceCount(player);
		
		// If player has 7+ resources, must build something
		boolean mustBuild = totalResources >= MAX_RESOURCES_BEFORE_BUILD;
		
		// Get available actions
		List<String> availableActions = getAvailableActions(player, mustBuild);
		
		if (availableActions.isEmpty()) {
			System.out.println(player.getColor() + " - No available actions");
			return;
		}
		
		// Choose randomly from available actions
		String action = availableActions.get(random.nextInt(availableActions.size()));
		
		switch (action) {
			case "SETTLEMENT":
				buildSettlement(player, playerIndex);
				break;
			case "CITY":
				buildCity(player, playerIndex);
				break;
			case "ROAD":
				buildRoad(player, playerIndex);
				break;
			case "PASS":
				System.out.println(roundCount + " / " + player.getColor() + ": Pass");
				break;
		}
	}

	/**
	 * Gets available actions for a player based on their resources and board state.
	 * @param player The player to check actions for
	 * @param mustBuild Whether the player is forced to build (7+ resources)
	 * @return List of available action strings
	 */
	private List<String> getAvailableActions(Player player, boolean mustBuild) {
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
	 * @param playerIndex The index of the player in the players array
	 */
	private void buildSettlement(Player player, int playerIndex) {
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
	 * @param playerIndex The index of the player in the players array
	 */
	private void buildCity(Player player, int playerIndex) {
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
	 * @param playerIndex The index of the player in the players array
	 */
	private void buildRoad(Player player, int playerIndex) {
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
	private int getTotalResourceCount(Player player) {
		return player.getTotalResourceCount();
	}

	/**
	 * Distributes resources to all players based on the dice roll.
	 * Each player with a settlement/city on a tile matching the rolled number receives resources.
	 * Cities produce double resources.
	 * @param diceRoll The number rolled on the dice (2-12)
	 */
	private void distributeResources(int diceRoll) {
		// Skip 7 (robber - no resources distributed)
		if (diceRoll == 7) {
			return;
		}
		
		// Process all tiles on the board
		for (Tile tile : board.getTiles()) {
			if (tile.getNumber() == diceRoll) {
				ResourceType resource = tile.produceResource();
				if (resource == ResourceType.NULL) continue;
				
				// Check all nodes touching this tile
				int[] nodeIds = tile.getNodeIds();
				for (int nodeId : nodeIds) {
					Node node = board.getNode(nodeId);
					if (node != null && node.isOccupied()) {
						Player owner = node.getOccupyingPlayer();
						if (owner != null) {
							// Use resource multiplier from building (1 for settlement, 2 for city)
							int amount = node.getBuilding().getResourceMultiplier();
							for (int i = 0; i < amount; i++) {
								owner.addResource(resource);
							}
							if (amount > 1) {
								System.out.println(roundCount + " / " + owner.getColor() + ": Received 2x " + resource);
							} else {
								System.out.println(roundCount + " / " + owner.getColor() + ": Received " + resource);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Gets all available nodes for settlement placement (setup phase - distance rule only).
	 * @return List of node IDs where a settlement can be placed
	 */
	private List<Integer> getAvailableSettlementNodes() {
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
	private List<Integer> getAvailableSettlementNodesForPlayer(Player player) {
		List<Integer> available = new ArrayList<>();
		
		// Check all nodes on the board
		for (int i = 0; i < TOTAL_NODES; i++) {
			Node node = board.getNode(i);
			if (node != null && validator.canPlaceSettlement(node, player, false) && player.canBuildSettlement()) {
				// Check if player has a road leading to this node
				boolean hasRoadConnection = false;
				for (Edge edge : board.getEdges()) {
					if (edge != null && edge.touches(node) && edge.getRoad() != null && edge.getRoad().getOwner() == player) {
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
	private List<Integer> getUpgradeableCityNodes(Player player) {
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
	private List<Edge> getAvailableRoadEdgesForPlayer(Player player) {
		List<Edge> available = new ArrayList<>();
		
		// Get all edges that can be reached from player's buildings or roads
		for (Edge edge : board.getEdges()) {
			if (edge != null && edge.canPlaceRoad() && player.canBuildRoad()) {
				Node nodeA = edge.getNodeA();
				Node nodeB = edge.getNodeB();
				
				// Check if edge is adjacent to player's building
				boolean adjacentToBuilding = (nodeA.isOccupied() && nodeA.getOccupyingPlayer() == player) ||
											(nodeB.isOccupied() && nodeB.getOccupyingPlayer() == player);
				
				// Check if edge is adjacent to player's road
				boolean adjacentToRoad = false;
				for (Edge otherEdge : board.getEdges()) {
					if (otherEdge != null && otherEdge != edge && otherEdge.getRoad() != null && 
						otherEdge.getRoad().getOwner() == player) {
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
	 * Gets the index of a player in the players array.
	 * @param player the player to find
	 * @return the index of the player, or -1 if not found
	 */
	private int getPlayerNumber(Player player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] == player) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets all node IDs that the player currently occupies (settlements/cities).
	 * @param player the player
	 * @return list of occupied node IDs
	 */
	private List<Integer> getOccupiedNodeIds(Player player) {

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
	private List<Integer> getAdjacentUnoccupiedNodeIds(int nodeId) {

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

	/**
	 * Checks if the game is over for a specific player (they reached victory point goal).
	 * @param player The player to check
	 * @return true if player has reached victory point goal, false otherwise
	 */
	public boolean isGameOver(Player player) {
		if (player.getVictoryPoints() >= VICTORY_POINTS_TO_WIN){
			return true;
		}
		return false;
	}

	/**
	 * Gets the winner of the game (first player to reach victory point goal).
	 * @return The winning player, or null if no winner yet
	 */
	public Player getWinner() {
		for (Player p: players){
			if (isGameOver(p)){
				return p;
			}
		}
		return null;
	}

	/**
	 * Orchestrates settlement placement during setup (no resource cost).
	 * GRASP: Controller - Game orchestrates the full placement flow.
	 * @param node The node where the settlement will be placed
	 * @param player The player placing the settlement
	 * @return true if placement succeeded, false otherwise
	 */
	private boolean placeSettlementSetup(Node node, Player player) {
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
	 * GRASP: Controller - Game orchestrates the full placement flow.
	 * @param node The node where the settlement will be placed
	 * @param player The player placing the settlement
	 * @return true if placement succeeded, false otherwise
	 */
	private boolean placeSettlement(Node node, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceSettlement(node, player, false)) {
			return false;
		}

		// Check road connectivity (normal play requirement)
		boolean hasRoadConnection = false;
		for (Edge edge : board.getEdges()) {
			if (edge != null && edge.touches(node) && edge.getRoad() != null && edge.getRoad().getOwner() == player) {
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
	 * GRASP: Controller - Game orchestrates the full placement flow.
	 * @param node The node where the city will be placed (must have a settlement)
	 * @param player The player placing the city
	 * @return true if placement succeeded, false otherwise
	 */
	private boolean placeCity(Node node, Player player) {
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
	 * GRASP: Controller - Game orchestrates the full placement flow.
	 * @param edge The edge where the road will be placed
	 * @param player The player placing the road
	 * @return true if placement succeeded, false otherwise
	 */
	private boolean placeRoadSetup(Edge edge, Player player) {
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
	 * GRASP: Controller - Game orchestrates the full placement flow.
	 * @param edge The edge where the road will be placed
	 * @param player The player placing the road
	 * @return true if placement succeeded, false otherwise
	 */
	private boolean placeRoad(Edge edge, Player player) {
		// 1. Validate placement rules
		if (!validator.canPlaceRoad(edge, player, false)) {
			return false;
		}

		// Check connectivity (must be adjacent to player's building or road)
		Node nodeA = edge.getNodeA();
		Node nodeB = edge.getNodeB();
		boolean adjacentToBuilding = (nodeA.isOccupied() && nodeA.getOccupyingPlayer() == player) ||
									(nodeB.isOccupied() && nodeB.getOccupyingPlayer() == player);

		boolean adjacentToRoad = false;
		for (Edge otherEdge : board.getEdges()) {
			if (otherEdge != null && otherEdge != edge && otherEdge.getRoad() != null &&
				otherEdge.getRoad().getOwner() == player) {
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
}
