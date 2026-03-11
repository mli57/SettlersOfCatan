import org.junit.*;

import SettlersOfCatan.Edge;
import SettlersOfCatan.Node;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.Road;

/**
 * Unit tests for the {@link Edge} class.
 * Covers canPlaceRoad, touches, isAdjacentTo, and getOccupyingPlayer.
 */
public class EdgeTest {

	private Node nodeA;
	private Node nodeB;
	private Node nodeC;
	private Edge edgeAB;
	private Player player;

	@BeforeEach
	public void setUp() {
		nodeA = new Node(0);
		nodeB = new Node(1);
		nodeC = new Node(2);
		edgeAB = new Edge(0, nodeA, nodeB);
		player = new Player(PlayerColor.RED);
	}

	@Test
	public void canPlaceRoadReturnsTrueWhenEmptyFalseWhenOccupied() {
		assertTrue(edgeAB.canPlaceRoad());

		Road road = new Road(player, edgeAB);
		edgeAB.setRoad(road);

		assertFalse(edgeAB.canPlaceRoad());
	}

	@Test
	public void touchesReturnsTrueForEndpointsFalseForOtherAndNull() {
		assertTrue(edgeAB.touches(nodeA));
		assertTrue(edgeAB.touches(nodeB));
		assertFalse(edgeAB.touches(nodeC));
		assertFalse(edgeAB.touches(null));
	}

	@Test
	public void isAdjacentToTrueWhenSharingNodeFalseWhenSameOrDisjointOrNull() {
		Edge edgeBC = new Edge(1, nodeB, nodeC);

		assertTrue(edgeAB.isAdjacentTo(edgeBC));
		assertTrue(edgeBC.isAdjacentTo(edgeAB));

		assertFalse(edgeAB.isAdjacentTo(edgeAB));
		assertFalse(edgeAB.isAdjacentTo(null));

		Node nodeD = new Node(3);
		Edge edgeCD = new Edge(2, nodeC, nodeD);
		assertFalse(edgeAB.isAdjacentTo(edgeCD));
	}

	@Test
	public void getOccupyingPlayerNullWhenNoRoadReturnsOwnerWhenRoadPlaced() {
		assertNull(edgeAB.getOccupyingPlayer());

		Road road = new Road(player, edgeAB);
		edgeAB.setRoad(road);

		assertSame(player, edgeAB.getOccupyingPlayer());
	}
}
