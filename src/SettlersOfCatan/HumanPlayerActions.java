package SettlersOfCatan;

import static SettlersOfCatan.HumanCommandParser.Action.*;

import java.util.Random;
import java.util.Scanner;
import java.util.function.IntConsumer;

/**
 * Handles the interactive console turn for a {@link HumanPlayer}.
 * Extends {@link PlayerActions} so it can call the inherited placement methods
 * ({@code placeSettlement}, {@code placeCity}, {@code placeRoad}) directly
 * without duplicating logic.
 * <p>
 * Satisfies the GRASP principle of Single Responsibility: this class is solely
 * responsible for interpreting human player console input and delegating each
 * command to the appropriate placement or game operation.
 *
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class HumanPlayerActions extends PlayerActions {

	/** Scanner for reading human player input from console */
	private final Scanner scanner;

	/** Dice object for rolling dice during human turn */
	private final Dice dice;

	/** Number of sides on each die */
	private final int diceSides;

	/** Consumer to invoke resource distribution with a given dice roll */
	private final IntConsumer distributeResources;

	/**
	 * Constructs a HumanPlayerActions instance.
	 * @param board The game board
	 * @param bank The bank for handling payments
	 * @param validator The placement validator
	 * @param players Array of all players (shared reference)
	 * @param random Random number generator (passed to parent for AI helpers)
	 * @param scanner Scanner for reading console input
	 * @param dice Dice object for rolling
	 * @param diceSides Number of sides on each die
	 * @param distributeResources Callback to distribute resources for a given roll
	 */
	public HumanPlayerActions(Board board, Bank bank, IPlacementValidator validator,
			Player[] players, Random random, Scanner scanner, Dice dice, int diceSides,
			IntConsumer distributeResources) {
		super(board, bank, validator, players, random);
		this.scanner = scanner;
		this.dice = dice;
		this.diceSides = diceSides;
		this.distributeResources = distributeResources;
	}

	/**
	 * Human turn loop: reads console commands until "go" is typed.
	 * @param player the player whose turn it is
	 * @param roundCount the current round number (for logging)
	 */
	public void humanTurn(HumanPlayer player, int roundCount) {
		boolean rolled = false;
		System.out.println("Your hand: " + player.formatHand());
		System.out.println("Commands: roll | list | build settlement <id> | build city <id> | build road <fromId>,<toId> | go");
		while (true) {
			System.out.print("> ");
			HumanCommandParser.ParsedCommand cmd = HumanCommandParser.parse(scanner.nextLine());
			switch (cmd.getAction()) {
				case ROLL:
					if (rolled) {
						System.out.println("Already rolled.");
						break;
					}
					int roll = dice.rollTwoDice(diceSides);
					System.out.println(roundCount + " / " + player.getColor() + ": Rolled " + roll);
					distributeResources.accept(roll);
					rolled = true;
					break;
				case LIST:
					System.out.println("Hand: " + player.formatHand());
					break;
				case BUILD_SETTLEMENT:
					if (!rolled) {
						System.out.println("Roll first.");
						break;
					}
					Node sNode = board.getNode(cmd.getNodeId());
					if (sNode == null) {
						System.out.println("Invalid node.");
						break;
					}
					if (placeSettlement(sNode, player)) {
						System.out.println("Settlement built on node " + cmd.getNodeId());
					} else {
						System.out.println("Cannot build there.");
					}
					break;
				case BUILD_CITY:
					if (!rolled) {
						System.out.println("Roll first.");
						break;
					}
					Node cNode = board.getNode(cmd.getNodeId());
					if (cNode == null) {
						System.out.println("Invalid node.");
						break;
					}
					if (placeCity(cNode, player)) {
						System.out.println("City built on node " + cmd.getNodeId());
					} else {
						System.out.println("Cannot build there.");
					}
					break;
				case BUILD_ROAD:
					if (!rolled) {
						System.out.println("Roll first.");
						break;
					}
					Edge edge = board.findEdge(cmd.getFromNodeId(), cmd.getToNodeId());
					if (edge == null) {
						System.out.println("No edge between those nodes.");
						break;
					}
					if (placeRoad(edge, player)) {
						System.out.println("Road built.");
					} else {
						System.out.println("Cannot build there.");
					}
					break;
				case GO:
					if (!rolled) {
						System.out.println("You must roll first.");
						break;
					}
					System.out.println(roundCount + " / " + player.getColor() + ": End turn.");
					return;
				default: System.out.println("Unknown command.");
			}
		}
	}
}
