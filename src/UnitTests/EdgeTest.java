
/**
 * Tests for the Edge class in our Settlers of Catan game.
 * Checks road placement, node touching, adjacency, and player ownership.
 * @author Kabir Singh Sachdeva, Sarthak Kulashari
 */

package UnitTests;

import static org.junit.jupiter.api.Assertions.*;       
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import SettlersOfCatan.Edge;
import SettlersOfCatan.Node;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.Road;

/**
 * Unit tests for the Edge class.
 * Checks road placement, node touching, adjacency, and player ownership.
 */
public class EdgeTest {

	/* Object variable for node a */
    private Node nodeA;

	/* Object variable for node b */
    private Node nodeB;

	/* Object variable for node c */
    private Node nodeC;

	/* Object variable for edge of node (A-B) */
    private Edge edgeAB;

	/* Object variable for player */
    private Player player;

	/* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

	/**
	 * Setups the objects before each test
	 */
    @BeforeEach
    public void setUp() {
        nodeA = new Node(0);
        nodeB = new Node(1);
        nodeC = new Node(2);
        edgeAB = new Edge(0, nodeA, nodeB);
        player = new Player(PlayerColor.RED);
    }

    /**
     * Test 1: canPlaceRoad returns true when the edge is empty, false when occupied.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceRoadReturnsTrueWhenEmptyFalseWhenOccupied() {
        assertTrue(edgeAB.canPlaceRoad(), "Road should be placeable on empty edge");

        Road road = new Road(player, edgeAB);
        edgeAB.setRoad(road);

        assertFalse(edgeAB.canPlaceRoad(), "Road should not be placeable on occupied edge");
    }

    /**
     * Test 2: touches returns true for the edge's endpoints, false for other nodes or null.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void touchesReturnsTrueForEndpointsFalseForOtherAndNull() {
        assertTrue(edgeAB.touches(nodeA), "Edge should touch its first endpoint");
        assertTrue(edgeAB.touches(nodeB), "Edge should touch its second endpoint");
        assertFalse(edgeAB.touches(nodeC), "Edge should not touch an unrelated node");
        assertFalse(edgeAB.touches(null), "Edge should not touch null");
    }

    /**
     * Test 3: isAdjacentTo returns true when edges share a node, false when same, disjoint, or null.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void isAdjacentToTrueWhenSharingNodeFalseWhenSameOrDisjointOrNull() {
        Edge edgeBC = new Edge(1, nodeB, nodeC);

        assertTrue(edgeAB.isAdjacentTo(edgeBC), "Edges sharing a node should be adjacent");
        assertTrue(edgeBC.isAdjacentTo(edgeAB), "Adjacency should be symmetric");

        assertFalse(edgeAB.isAdjacentTo(edgeAB), "Edge should not be adjacent to itself");
        assertFalse(edgeAB.isAdjacentTo(null), "Edge should not be adjacent to null");

        Node nodeD = new Node(3);
        Edge edgeCD = new Edge(2, nodeC, nodeD);
        assertFalse(edgeAB.isAdjacentTo(edgeCD), "Disjoint edges should not be adjacent");
    }

    /**
     * Test 4: getOccupyingPlayer returns null when no road, returns owner when road placed.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void getOccupyingPlayerNullWhenNoRoadReturnsOwnerWhenRoadPlaced() {
        assertNull(edgeAB.getOccupyingPlayer(), "No player should occupy an empty edge");

        Road road = new Road(player, edgeAB);
        edgeAB.setRoad(road);

        assertSame(player, edgeAB.getOccupyingPlayer(), "Player should occupy the edge after placing road");
    }
}