/**
 * Tests for the PlacementValidator class in our Settlers of Catan game.
 * Checks setup phase rules and basic placement for settlements, roads, and cities.
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
import SettlersOfCatan.PlacementValidator;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.Road;
import SettlersOfCatan.Settlement;

/**
 * Tests for the PlacementValidator class.
 * Checks setup phase rules and basic placement for settlements, roads, and cities.
 */
public class PlacementValidatorTest {

    /* PlacementValidator object variable */
    private PlacementValidator validator;

    /* Player object variable */
    private Player player;

	/* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

	/**
	 * Setups the objects before each test
	 */
    @BeforeEach
    public void setUp() {
        validator = new PlacementValidator();
        player = new Player(PlayerColor.RED);
    }

    /**
     * Test 1: canPlaceSettlement returns false when the node is null.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceSettlementReturnsFalseForNullNode() {
        assertFalse(validator.canPlaceSettlement(null, player, false), "Settlement should not be placeable on a null node");
        assertFalse(validator.canPlaceSettlement(null, null, true), "Settlement should not be placeable on a null node in setup phase");
    }

    /**
     * Test 2: Setup-phase settlement: when player is null, only the node's distance rule is checked.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceSettlementSetupPhaseIgnoresPlayerAndUsesDistanceRule() {
        Node node = new Node(0);
        Node adjacent = new Node(1);
        node.addAdjacentNode(adjacent);

        /* Adjacent has a building so the distance rule should fail */
        Player dummyPlayer = new Player(PlayerColor.BLUE);
        adjacent.setBuilding(new Settlement(dummyPlayer));

        assertFalse(validator.canPlaceSettlement(node, null, true), "Settlement should not be placeable due to distance rule violation");

        /* Empty node with non-null player in setup phase should succeed */
        Node emptyNode = new Node(5);
        assertTrue(validator.canPlaceSettlement(emptyNode, player, true), "Settlement should be placeable in setup phase on empty node with player");
    }

    /**
     * Test 3: Setup-phase road: road must touch a node with the player's settlement.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceRoadSetupPhaseRequiresAdjacentSettlementOfPlayer() {
        Node nodeA = new Node(1);
        Node nodeB = new Node(2);
        Edge edge = new Edge(0, nodeA, nodeB);

        /* Give player a settlement on nodeA */
        nodeA.setBuilding(new Settlement(player));
        nodeA.setOccupyingPlayer(player);

        assertTrue(validator.canPlaceRoad(edge, player, true), "Road should be placeable when touching player's settlement in setup phase");

        /* Null edge and null player should both fail */
        assertFalse(validator.canPlaceRoad(null, player, true), "Road should not be placeable on a null edge");
        assertFalse(validator.canPlaceRoad(edge, null, true), "Road should not be placeable for a null player");

        /* Already occupied edge should fail */
        Road road = new Road(player, edge);
        edge.setRoad(road);
        assertFalse(validator.canPlaceRoad(edge, player, true), "Road should not be placeable on an already occupied edge");

        /* Normal play phase returns true (connectivity is checked by Game) */
        Edge freshEdge = new Edge(1, new Node(3), new Node(4));
        assertTrue(validator.canPlaceRoad(freshEdge, player, false), "Road should be placeable in normal play phase on empty edge");
    }

    /**
     * Test 4: canPlaceCity requires an existing settlement owned by the player;
     * fails for empty nodes, null inputs, and settlements owned by another player.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceCityRequiresPlayersSettlementAndFailsOtherwise() {
        Node node = new Node(3);

        /* Success: node has this player's settlement */
        node.setBuilding(new Settlement(player));
        node.setOccupyingPlayer(player);
        assertTrue(validator.canPlaceCity(node, player), "City should be placeable on player's existing settlement");

        /* Fail: wrong player owns the settlement */
        Player otherPlayer = new Player(PlayerColor.BLUE);
        assertFalse(validator.canPlaceCity(node, otherPlayer), "City should not be placeable on another player's settlement");

        /* Fail: null inputs */
        assertFalse(validator.canPlaceCity(null, player), "City should not be placeable on a null node");
        assertFalse(validator.canPlaceCity(node, null), "City should not be placeable for a null player");
    }
}
