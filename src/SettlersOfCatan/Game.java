package SettlersOfCatan;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main game controller that orchestrates the Settlers of Catan game flow.
 * Handles setup phase, game loop, resource distribution, and player actions.
 * Delegates all placement operations and board queries to {@link PlayerActions},
 * and console input handling for human players to {@link HumanPlayerActions}.
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

	/** Scanner for reading human player input from console **/
	private final Scanner scanner = new Scanner(System.in);

	/** Victory points needed to win the game **/
	private static final int VICTORY_POINTS_TO_WIN = 10;

	/** Maximum resources a player can have before being forced to build **/
	private static final int MAX_RESOURCES_BEFORE_BUILD = 7;

	/** Number of sides on each die **/
	private static final int DICE_SIDES = 6;

	/** Total number of nodes on the Catan board **/
	private static final int TOTAL_NODES = 54;

	/** Encapsulates placement operations and board-query helpers for AI and setup **/
	private PlayerActions actions;

	/** Handles the interactive console turn for human players **/
	private HumanPlayerActions humanActions;

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

		this.actions = new PlayerActions(board, bank, validator, players, random);
		this.humanActions = new HumanPlayerActions(board, bank, validator, players, random,
				scanner, dice, DICE_SIDES, this::distributeResources);
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
	 * Replaces the player at the given index with a HumanPlayer.
	 * Call before startGame().
	 * @param index 0-3 corresponding to PlayerColor order
	 */
	public void setHumanPlayer(int index) {
		if (index < 0 || index >= players.length) return;
		players[index] = new HumanPlayer(PlayerColor.values()[index]);
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
			doSetupSettlement(player, 1);

			// Place first road
			doSetupRoad(player, 1);

			// Place second settlement
			doSetupSettlement(player, 2);

			// Place second road
			doSetupRoad(player, 2);
		}
	}

	/**
	 * Places one settlement for a player during setup (random choice).
	 * Delegates board queries and placement to {@link PlayerActions}.
	 * @param player The player placing the settlement
	 * @param settlementNumber Which settlement this is (1 or 2)
	 */
	private void doSetupSettlement(Player player, int settlementNumber) {
		// Get all available nodes (following distance rule)
		List<Integer> availableNodes = actions.getAvailableSettlementNodes();

		if (availableNodes.isEmpty()) {
			System.out.println(player.getColor() + " - No available nodes for settlement #" + settlementNumber);
			return;
		}

		// Choose randomly from available nodes
		int nodeId = availableNodes.get(random.nextInt(availableNodes.size()));
		Node node = board.getNode(nodeId);

		if (node != null && actions.placeSettlementSetup(node, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Placed settlement #" + settlementNumber + " on node " + nodeId);
		}
	}

	/**
	 * Places one road for a player during setup (random choice).
	 * Delegates board queries and placement to {@link PlayerActions}.
	 * @param player The player placing the road
	 * @param roadNumber Which road this is (1 or 2)
	 */
	private void doSetupRoad(Player player, int roadNumber) {
		// Get all nodes the player occupies
		List<Integer> occupiedNodes = actions.getOccupiedNodeIds(player);

		if (occupiedNodes.isEmpty()) {
			System.out.println(player.getColor() + " - No settlements to build roads from");
			return;
		}

		// Choose random starting node from player's settlements
		int firstNodeId = occupiedNodes.get(random.nextInt(occupiedNodes.size()));

		// Get adjacent unoccupied nodes
		List<Integer> adjacentUnoccupied = actions.getAdjacentUnoccupiedNodeIds(firstNodeId);

		if (adjacentUnoccupied.isEmpty()) {
			// Try another occupied node if first one has no adjacent unoccupied nodes
			for (int occupiedId : occupiedNodes) {
				if (occupiedId != firstNodeId) {
					adjacentUnoccupied = actions.getAdjacentUnoccupiedNodeIds(occupiedId);
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
		if (edge != null && actions.placeRoadSetup(edge, player)) {
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
	 * Handles a player's turn.
	 * If the player is a HumanPlayer, delegates to {@link HumanPlayerActions#humanTurn}.
	 * Otherwise the agent picks randomly from available actions via {@link PlayerActions}.
	 * @param player The player whose turn it is
	 * @param playerIndex The index of the player in the players array
	 */
	private void playerTurn(Player player, int playerIndex) {
		if (player instanceof HumanPlayer) {
			humanActions.humanTurn((HumanPlayer) player, roundCount);
			return;
		}

		// AI path: delegate entirely to PlayerActions
		boolean mustBuild = actions.getTotalResourceCount(player) >= MAX_RESOURCES_BEFORE_BUILD;
		List<String> availableActions = actions.getAvailableActions(player, mustBuild);

		if (availableActions.isEmpty()) {
			System.out.println(player.getColor() + " - No available actions");
			return;
		}

		// Choose randomly from available actions
		String action = availableActions.get(random.nextInt(availableActions.size()));

		switch (action) {
			case "SETTLEMENT":
				actions.buildSettlement(player, roundCount);
				break;
			case "CITY":
				actions.buildCity(player, roundCount);
				break;
			case "ROAD":
				actions.buildRoad(player, roundCount);
				break;
			case "PASS":
				System.out.println(roundCount + " / " + player.getColor() + ": Pass");
				break;
		}
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
}
