
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
     * Test 1: Setup-phase settlement: when player is null, only the node's distance rule should be considered.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceSettlementSetupPhaseIgnoresPlayerAndUsesDistanceRule() {
        Node node = new Node(0);
        Node adjacent = new Node(1);
        node.addAdjacentNode(adjacent);

        // Adjacent has a building, so distance rule fails
        Player dummyPlayer = new Player(PlayerColor.BLUE);  
        adjacent.setBuilding(new Settlement(dummyPlayer));  

        assertFalse(validator.canPlaceSettlement(node, null, true), "Settlement should not be placeable due to distance rule violation");
    }

    /**
     * Test 2: Setup-phase road: road must touch a node with the player's settlement.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceRoadSetupPhaseRequiresAdjacentSettlementOfPlayer() {
        Node nodeA = new Node(1);
        Node nodeB = new Node(2);
        Edge edge = new Edge(0, nodeA, nodeB);

        // Give player a settlement on nodeA
        nodeA.setBuilding(new Settlement(player));  // Fixed: Pass the player to the constructor
        nodeA.setOccupyingPlayer(player);

        assertTrue(validator.canPlaceRoad(edge, player, true), "Road should be placeable when touching player's settlement in setup phase");
    }

    /**
     * Test 3: City placement: requires an existing settlement owned by the player.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceCityRequiresPlayersSettlementOnNode() {
        Node node = new Node(3);

        // Settlement belonging to this player
        node.setBuilding(new Settlement(player));  // Fixed: Pass the player to the constructor
        node.setOccupyingPlayer(player);

        assertTrue(validator.canPlaceCity(node, player), "City should be placeable on player's existing settlement");
    }

    /**
     * Test 4: City placement fails when there is no building on the node.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceCityFailsWhenNoSettlementPresent() {
        Node node = new Node(4);

        assertFalse(validator.canPlaceCity(node, player), "City should not be placeable on an empty node");
    }
}