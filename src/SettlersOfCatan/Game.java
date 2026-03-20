package SettlersOfCatan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

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

	/** Scanner for reading human player input from console **/
	private final Scanner scanner = new Scanner(System.in);

	/** Index of the tile currently holding the Robber (R2.5); -1 means unset. **/
	private int robberTileIndex = -1;

	/** Victory points needed to win the game **/
	private static final int VICTORY_POINTS_TO_WIN = 10;

	/** Number of sides on each die **/
	private static final int DICE_SIDES = 6;

	/** Path to visualizer base map JSON (board layout). **/
	private static final String VISUALIZER_BASE_MAP_PATH = "src/SettlersOfCatan/visualize/base_map.json";
	/** Path to visualizer state JSON (roads and buildings). **/
	private static final String VISUALIZER_STATE_PATH = "src/SettlersOfCatan/visualize/state.json";

	/** Actions handler for AI and common placement operations **/
	private PlayerActions actions;

	/** Actions handler for human player interactive turns **/
	private HumanPlayerActions humanActions;

	/** Rule-based AI action chain (Chain of Responsibility). **/
	private ActionHandler agentChain;

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
		this.roundCount = 1;

		// Initialize players
		initializePlayers();

		// Initialize action handlers
		this.actions = new PlayerActions(board, bank, validator, players, random);
		this.humanActions = new HumanPlayerActions(board, bank, validator, players, random,
				scanner, dice, DICE_SIDES, this::distributeResources, this::handleRobber);
		buildAgentChain();
	}

	/**
	 * Builds the Chain of Responsibility for AI decisions.
	 * Order: OverHandSize -> ConnectRoads -> DefendRoad -> ValueScoring.
	 */
	private void buildAgentChain() {
		ActionHandler overHandSize = new OverHandSizeHandler(random);
		ActionHandler connectRoads = new ConnectRoadsHandler(board, random);
		ActionHandler defendRoad = new DefendRoadHandler(players, board, random);
		ActionHandler valueScoring = new ValueScoringHandler(random);

		overHandSize.setSuccessor(connectRoads);
		connectRoads.setSuccessor(defendRoad);
		defendRoad.setSuccessor(valueScoring);

		this.agentChain = overHandSize;
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
	 * Setup phase in two rounds.
	 * In each round, every player places one settlement and one road attached
	 * to that settlement: S1-R1 for all players, then S2-R2 for all players.
	 */
	public void setupInitialSettlements() {
		for (int setupRound = 1; setupRound <= 2; setupRound++) {
			for (int i = 0; i < players.length; i++) {
				Player player = players[i];
				System.out.println("\n=== " + player.getColor() + " Player Setup Round " + setupRound + " ===");

				if (player instanceof HumanPlayer) {
					setupInitialPlacementHuman((HumanPlayer) player, setupRound);
				} else {
					int settlementNodeId = aiSetupSettlement(player, setupRound);
					aiSetupRoadFromSettlement(player, setupRound, settlementNodeId);
				}
			}
		}
	}

	/**
	 * Places one settlement for an AI player during setup (random choice).
	 * @param player The player placing the settlement
	 * @param settlementNumber Which settlement this is (1 or 2)
	 * @return placed settlement node id, or -1 if placement failed
	 */
	private int aiSetupSettlement(Player player, int settlementNumber) {
		// Get all available nodes (following distance rule)
		List<Integer> availableNodes = actions.getAvailableSettlementNodes();

		if (availableNodes.isEmpty()) {
			System.out.println(player.getColor() + " - No available nodes for settlement #" + settlementNumber);
			return -1;
		}

		// Choose randomly from available nodes
		int nodeId = availableNodes.get(random.nextInt(availableNodes.size()));
		Node node = board.getNode(nodeId);

		if (node != null && actions.placeSettlementSetup(node, player)) {
			System.out.println(roundCount + " / " + player.getColor() + ": Placed settlement #" + settlementNumber + " on node " + nodeId);
			return nodeId;
		}
		return -1;
	}

	/**
	 * Places one road for an AI player during setup that must connect to the
	 * settlement placed in the same setup turn.
	 * @param player The player placing the road
	 * @param roadNumber Which road this is (1 or 2)
	 * @param settlementNodeId node id of the settlement placed immediately before this road
	 */
	private void aiSetupRoadFromSettlement(Player player, int roadNumber, int settlementNodeId) {
		if (settlementNodeId < 0) {
			System.out.println(player.getColor() + " - Cannot place road #" + roadNumber + " (no settlement placed)");
			return;
		}
		int firstNodeId = settlementNodeId;
		List<Integer> adjacentUnoccupied = actions.getAdjacentUnoccupiedNodeIds(firstNodeId);

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
	 * Setup placement for a human player for one setup round:
	 * place one settlement and then one road attached to that settlement.
	 * @param player the human player setting up
	 * @param placementNumber setup round number (1 or 2)
	 */
	private void setupInitialPlacementHuman(HumanPlayer player, int placementNumber) {
		int placedSettlementNodeId = -1;
		// Settlement
		while (true) {
			System.out.println("Place settlement #" + placementNumber + " (command: build settlement <nodeId>):");
			System.out.print("> ");
			HumanCommandParser.ParsedCommand cmd = HumanCommandParser.parse(scanner.nextLine());
			if (cmd.getAction() != HumanCommandParser.Action.BUILD_SETTLEMENT) {
				System.out.println("Please use: build settlement <nodeId>");
				continue;
			}
			Node node = board.getNode(cmd.getNodeId());
			if (node == null) {
				System.out.println("Invalid node.");
				continue;
			}
			if (actions.placeSettlementSetup(node, player)) {
				placedSettlementNodeId = cmd.getNodeId();
				System.out.println("Settlement built on node " + cmd.getNodeId());
				break;
			} else {
				System.out.println("Cannot build settlement there. Try another node.");
			}
		}

		// Road (must connect to the settlement just placed)
		while (true) {
			System.out.println("Place road #" + placementNumber + " from settlement node " + placedSettlementNodeId
					+ " (command: build road <fromNodeId>,<toNodeId>):");
			System.out.print("> ");
			HumanCommandParser.ParsedCommand cmd = HumanCommandParser.parse(scanner.nextLine());
			if (cmd.getAction() != HumanCommandParser.Action.BUILD_ROAD) {
				System.out.println("Please use: build road <fromNodeId>,<toNodeId>");
				continue;
			}
			Edge edge = board.findEdge(cmd.getFromNodeId(), cmd.getToNodeId());
			if (edge == null) {
				System.out.println("No edge between those nodes.");
				continue;
			}
			Node justPlaced = board.getNode(placedSettlementNodeId);
			if (justPlaced == null || !edge.touches(justPlaced)) {
				System.out.println("Road must connect to settlement node " + placedSettlementNodeId + ".");
				continue;
			}
			if (actions.placeRoadSetup(edge, player)) {
				System.out.println("Road built from node " + cmd.getFromNodeId() + " to node " + cmd.getToNodeId());
				break;
			} else {
				System.out.println("Cannot build road there. Try another pair of nodes.");
			}
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
		// Write base map once so visualizer can render the board (R2.3)
		try {
			JsonWriter.writeBaseMap(board, VISUALIZER_BASE_MAP_PATH);
		} catch (IOException e) {
			System.err.println("Failed to write base_map.json: " + e.getMessage());
		}

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

				// Human players roll via the "roll" console command inside humanTurn.
				// AI players have the dice rolled automatically here.
				if (!(player instanceof HumanPlayer)) {
					int diceRoll = dice.rollTwoDice(DICE_SIDES);
					System.out.println("Dice roll: " + diceRoll);

					if (diceRoll == 7) {
						handleRobber(player);
					} else {
						distributeResources(diceRoll);
					}
				}

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
	 * If the player is a HumanPlayer, reads commands from the console.
	 * Otherwise the agent picks randomly from available actions.
	 * @param player The player whose turn it is
	 * @param playerIndex The index of the player in the players array
	 */
	private void playerTurn(Player player, int playerIndex) {
		if (player instanceof HumanPlayer) {
			humanActions.humanTurn((HumanPlayer) player, roundCount);
			return;
		}
		agentChain.handleTurn(player, actions, roundCount);
	}

	/**
	 * Handles the Robber mechanism for a roll of 7 (R2.5) with assignment simplifications.
	 * 1) Players with more than 7 cards discard down to 7 (randomly).
	 * 2) Robber moves to a random tile (different from current).
	 * 3) Active player steals one random card from a random qualifying adjacent player.
	 * @param activePlayer the player who rolled 7
	 */
	private void handleRobber(Player activePlayer) {

		// Step 1 — Card discard
		for (Player p : players) {
			int total = p.getTotalResourceCount();
			if (total <= 7) {
				continue;
			}

			int toDiscard = total - 7;
			List<ResourceType> cards = new ArrayList<>();
			for (Map.Entry<ResourceType, Integer> entry : p.getResources().entrySet()) {
				ResourceType type = entry.getKey();
				if (type == ResourceType.NULL) {
					continue;
				}
				for (int i = 0; i < entry.getValue(); i++) {
					cards.add(type);
				}
			}

			Collections.shuffle(cards, random);
			for (int i = 0; i < toDiscard && i < cards.size(); i++) {
				p.removeResource(cards.get(i), 1);
			}
		}

		// Step 2 — Robber placement
		Tile[] tiles = board.getTiles();
		if (tiles == null || tiles.length == 0) {
			return;
		}

		int newIndex = robberTileIndex;
		if (tiles.length == 1) {
			newIndex = 0;
		} else {
			while (newIndex == robberTileIndex) {
				newIndex = random.nextInt(tiles.length);
			}
		}

		robberTileIndex = newIndex;
		System.out.println(roundCount + " / ROBBER: Moved to tile " + robberTileIndex);

		// Step 3 — Steal a card
		Tile robberTile = tiles[robberTileIndex];
		int[] nodeIds = robberTile.getNodeIds();

		Set<Player> uniqueVictims = new HashSet<>();
		for (int nodeId : nodeIds) {
			Node node = board.getNode(nodeId);
			if (node == null) {
				continue;
			}
			if (!node.isOccupied()) {
				continue;
			}

			Player owner = node.getOccupyingPlayer();
			if (owner == null) {
				continue;
			}
			if (owner == activePlayer) {
				continue;
			}

			uniqueVictims.add(owner);
		}

		if (uniqueVictims.isEmpty()) {
			return;
		}

		List<Player> victims = new ArrayList<>(uniqueVictims);
		Player victim = victims.get(random.nextInt(victims.size()));

		List<ResourceType> victimCards = new ArrayList<>();
		for (Map.Entry<ResourceType, Integer> entry : victim.getResources().entrySet()) {
			ResourceType type = entry.getKey();
			if (type == ResourceType.NULL) {
				continue;
			}
			for (int i = 0; i < entry.getValue(); i++) {
				victimCards.add(type);
			}
		}

		if (victimCards.isEmpty()) {
			return;
		}

		ResourceType stolen = victimCards.get(random.nextInt(victimCards.size()));
		if (victim.removeResource(stolen, 1)) {
			activePlayer.addResource(stolen);
			System.out.println(roundCount + " / " + activePlayer.getColor() + ": Stole from " + victim.getColor());
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