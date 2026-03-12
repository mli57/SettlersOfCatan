/**
 * Tests for the player class in our Settlers of Catan game.
 * Checks resource accounting and affordability checks.
 * @author Kabir Singh Sachdeva, Sarthak Kulashari
 */

package UnitTests;
import static org.junit.jupiter.api.Assertions.*;       
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.ResourceType;

/**
 * Tests for the Player class.
 * Checks resource accounting and affordability checks.
 */
public class PlayerTest {

	/* Player object variable */
    private Player player;

	/* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

	/**
	 * Setups the objects before each test
	 */
    @BeforeEach
    public void setUp() {
        player = new Player(PlayerColor.RED);
    }

    /**
     * Test 1: Player can afford when they have at least the required resources for every type.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canAffordReturnsTrueWhenResourcesAreSufficient() {
        player.getResources().put(ResourceType.WOOD, 2);
        player.getResources().put(ResourceType.BRICK, 1);

        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.WOOD, 1);
        cost.put(ResourceType.BRICK, 1);

        assertTrue(player.canAfford(cost), "Player should afford when resources are sufficient");
    }

    /**
     * Test 2: Player cannot afford when one resource type is below the required amount.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canAffordReturnsFalseWhenOneResourceIsInsufficient() {
        player.getResources().put(ResourceType.WOOD, 0);
        player.getResources().put(ResourceType.BRICK, 2);

        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.WOOD, 1);
        cost.put(ResourceType.BRICK, 1);

        assertFalse(player.canAfford(cost), "Player should not afford when one resource is insufficient");
    }

    /**
     * Test 3: getTotalResourceCount handles zero resources and a small positive number.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void getTotalResourceCountHandlesZeroAndPositiveCounts() {
        assertEquals(0, player.getTotalResourceCount(), "Total resource count should be zero initially");

        player.getResources().put(ResourceType.WOOD, 2);
        player.getResources().put(ResourceType.BRICK, 3);

        assertEquals(5, player.getTotalResourceCount(), "Total resource count should be 5 after adding resources");
    }
}