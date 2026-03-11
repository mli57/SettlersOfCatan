import org.junit.*;

import java.util.EnumMap;
import java.util.Map;

import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.ResourceType;

/**
 * Unit tests for the {@link Player} class.
 * Focuses on resource accounting and affordability checks.
 */
public class PlayerTest {

	private Player player;

	@BeforeEach
	public void setUp() {
		player = new Player(PlayerColor.RED);
	}

	/**
	 * Partition test: player can afford when they have
	 * at least the required resources for every type.
	 */
	@Test
	public void canAffordReturnsTrueWhenResourcesAreSufficient() {
		player.getResources().put(ResourceType.WOOD, 2);
		player.getResources().put(ResourceType.BRICK, 1);

		Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
		cost.put(ResourceType.WOOD, 1);
		cost.put(ResourceType.BRICK, 1);

		assertTrue(player.canAfford(cost));
	}

	/**
	 * Partition test: player cannot afford when one resource
	 * type is below the required amount.
	 */
	@Test
	public void canAffordReturnsFalseWhenOneResourceIsInsufficient() {
		player.getResources().put(ResourceType.WOOD, 0);
		player.getResources().put(ResourceType.BRICK, 2);

		Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
		cost.put(ResourceType.WOOD, 1);
		cost.put(ResourceType.BRICK, 1);

		assertFalse(player.canAfford(cost));
	}

	/**
	 * Boundary test for getTotalResourceCount:
	 * zero resources and a small positive number.
	 */
	@Test
	public void getTotalResourceCountHandlesZeroAndPositiveCounts() {
		// Initially zero
		assertEquals(0, player.getTotalResourceCount());

		player.getResources().put(ResourceType.WOOD, 2);
		player.getResources().put(ResourceType.BRICK, 3);

		assertEquals(5, player.getTotalResourceCount());
	}
}