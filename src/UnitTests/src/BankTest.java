import org.junit.*;

import SettlersOfCatan.Bank;
import SettlersOfCatan.BuildingType;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.ResourceType;

/**
 * Unit tests for the {@link Bank} class.
 * These are intentionally limited to a small, meaningful subset
 * so that the overall test count stays within the assignment limit.
 */
public class BankTest {

	private Bank bank;
	private Player player;

	@BeforeEach
	public void setUp() {
		bank = new Bank();
		player = new Player(PlayerColor.RED);
	}

	/**
	 * Test 1 (partition / happy path):
	 * payForSettlement succeeds when the player has exactly the required resources
	 * and at least one settlement piece.
	 */
	@Test
	public void payForSettlementSucceedsWithExactResourcesAndPiece() {
		player.getResources().put(ResourceType.WOOD, 1);
		player.getResources().put(ResourceType.BRICK, 1);
		player.getResources().put(ResourceType.SHEEP, 1);
		player.getResources().put(ResourceType.WHEAT, 1);
		int initialSettlements = player.getBuildings().get(BuildingType.SETTLEMENT);
		int initialVictoryPoints = player.getVictoryPoints();

		boolean result = bank.payForSettlement(player);

		assertTrue(result);
		assertEquals(0, player.getResources().get(ResourceType.WOOD));
		assertEquals(0, player.getResources().get(ResourceType.BRICK));
		assertEquals(0, player.getResources().get(ResourceType.SHEEP));
		assertEquals(0, player.getResources().get(ResourceType.WHEAT));
		assertEquals(initialSettlements - 1, player.getBuildings().get(BuildingType.SETTLEMENT));
		assertEquals(initialVictoryPoints + 1, player.getVictoryPoints());
	}

	/**
	 * Test 2 (partition – insufficient resources):
	 * payForSettlement fails when the player does not have enough of at least one
	 * required resource type.
	 */
	@Test
	public void payForSettlementFailsWhenInsufficientResources() {
		int initialSettlements = player.getBuildings().get(BuildingType.SETTLEMENT);
		int initialVictoryPoints = player.getVictoryPoints();

		boolean result = bank.payForSettlement(player);

		assertFalse(result);
		assertEquals(initialSettlements, player.getBuildings().get(BuildingType.SETTLEMENT));
		assertEquals(initialVictoryPoints, player.getVictoryPoints());
	}

	/**
	 * Test 3:
	 * payForCity decreases resources correctly and does not drive any
	 * resource count below zero when the player has exactly the city cost.
	 */
	@Test
	public void payForCitySucceedsWithExactResourcesAndPiece() {
		player.getResources().put(ResourceType.ORE, 3);
		player.getResources().put(ResourceType.WHEAT, 2);
		int initialCities = player.getBuildings().get(BuildingType.CITY);
		int initialVictoryPoints = player.getVictoryPoints();

		boolean result = bank.payForCity(player);

		assertTrue(result);
		assertEquals(0, player.getResources().get(ResourceType.ORE));
		assertEquals(0, player.getResources().get(ResourceType.WHEAT));
		assertEquals(initialCities - 1, player.getBuildings().get(BuildingType.CITY));
		assertEquals(initialVictoryPoints + 1, player.getVictoryPoints());
	}

	/**
	 * Test 4:
	 * payForRoad fails when the player has the resources but no road pieces left.
	 */
	@Test
	public void payForRoadFailsWhenNoRoadPiecesLeft() {
		player.getResources().put(ResourceType.WOOD, 1);
		player.getResources().put(ResourceType.BRICK, 1);
		player.getBuildings().put(BuildingType.ROAD, 0);

		boolean result = bank.payForRoad(player);

		assertFalse(result);
		assertEquals(0, player.getBuildings().get(BuildingType.ROAD));
	}

	/**
	 * Test 5:
	 * useSettlementPieceSetup fails when the player has no settlement pieces left.
	 */
	@Test
	public void useSettlementPieceSetupFailsWhenNoPiecesLeft() {
		player.getBuildings().put(BuildingType.SETTLEMENT, 0);

		boolean result = bank.useSettlementPieceSetup(player);

		assertFalse(result);
		assertEquals(0, player.getBuildings().get(BuildingType.SETTLEMENT));
	}
}
