
/**
 * Tests for the Node class in our Settlers of Catan game.
 * Checks building placement rules, especially the distance rule.
 * @author Kabir Singh Sachdeva, Sarthak Kulashari
 */

package UnitTests;
import static org.junit.jupiter.api.Assertions.*;       
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import SettlersOfCatan.Node;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.Settlement;

/**
 * Tests for the Node class.
 * Checks building placement rules, especially the distance rule.
 */
public class NodeTest {

	/* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

    /**
     * Test 1: Isolated node with no building and no adjacent nodes should allow placing a building.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceBuildingReturnsTrueForIsolatedEmptyNode() {
        Node node = new Node(0);

        assertTrue(node.canPlaceBuilding(), "Node should allow building when isolated and empty");
    }

    /**
     * Test 2: Node with an adjacent node that already has a building should not allow placing a new building here.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceBuildingReturnsFalseWhenAdjacentNodeHasBuilding() {
        Node center = new Node(1);
        Node neighbor = new Node(2);

        center.addAdjacentNode(neighbor);
        neighbor.addAdjacentNode(center);

        Player dummyPlayer = new Player(PlayerColor.RED);
        neighbor.setBuilding(new Settlement(dummyPlayer));

        assertFalse(center.canPlaceBuilding(), "Node should not allow building when adjacent node has a building");
    }

    /**
     * Test 3: Node already occupied should not allow another building, even if all adjacent nodes are empty.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canPlaceBuildingReturnsFalseWhenNodeAlreadyHasBuilding() {
        Node node = new Node(3);

        Player anotherDummy = new Player(PlayerColor.BLUE);
        node.setBuilding(new Settlement(anotherDummy));

        assertFalse(node.canPlaceBuilding(), "Node should not allow building when already occupied");
    }
}