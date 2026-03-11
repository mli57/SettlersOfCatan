import org.junit.*;

import SettlersOfCatan.Edge;
import SettlersOfCatan.Node;
import SettlersOfCatan.PlacementValidator;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.Settlement;

/**
 * Unit tests for the {@link PlacementValidator} class.
 * Focuses on setup-phase behavior and basic city/road/settlement validity.
 */
public class PlacementValidatorTest {

	private PlacementValidator validator;
	private Player player;

	@BeforeEach
	public void setUp() {
		validator = new PlacementValidator();
		player = new Player(PlayerColor.RED);
	}

	/**
	 * Setup-phase settlement: when player is null, only the node's distance rule
	 * should be considered.
	 */
	@Test
	public void canPlaceSettlementSetupPhaseIgnoresPlayerAndUsesDistanceRule() {
		Node node = new Node(0);
		Node adjacent = new Node(1);
		node.addAdjacentNode(adjacent);

		// Adjacent has a building, so distance rule fails
		adjacent.setBuilding(new Settlement());

		assertFalse(validator.canPlaceSettlement(node, null, true));
	}

	/**
	 * Setup-phase road: road must touch a node with the player's settlement.
	 */
	@Test
	public void canPlaceRoadSetupPhaseRequiresAdjacentSettlementOfPlayer() {
		Node nodeA = new Node(1);
		Node nodeB = new Node(2);
		Edge edge = new Edge(0, nodeA, nodeB);

		// Give player a settlement on nodeA
		nodeA.setBuilding(new Settlement());
		nodeA.setOccupyingPlayer(player);

		assertTrue(validator.canPlaceRoad(edge, player, true));
	}

	/**
	 * City placement: requires an existing settlement owned by the player.
	 */
	@Test
	public void canPlaceCityRequiresPlayersSettlementOnNode() {
		Node node = new Node(3);

		// Settlement belonging to this player
		node.setBuilding(new Settlement());
		node.setOccupyingPlayer(player);

		assertTrue(validator.canPlaceCity(node, player));
	}

	/**
	 * City placement fails when there is no building on the node.
	 */
	@Test
	public void canPlaceCityFailsWhenNoSettlementPresent() {
		Node node = new Node(4);

		assertFalse(validator.canPlaceCity(node, player));
	}
}