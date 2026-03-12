/**
 * Tests for the Bank class in our Settlers of Catan game.
 * Checks how the bank handles buying settlements, cities, and roads.
 * @author Kabir Singh Sachdeva, Sarthak Kulashari
 */

package UnitTests;

import static org.junit.jupiter.api.Assertions.*;       
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import SettlersOfCatan.Bank;
import SettlersOfCatan.BuildingType;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.ResourceType;

/**
 * Unit tests for the Bank class.
 * Covers key scenarios for buying buildings and using pieces.
 */
public class BankTest {

	/* Bank variable object */
    private Bank bank;

	/* Player variable object */
    private Player player;

	/* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

	/**
	 * Setups the objects before each test
	 */
    @BeforeEach
    public void setUp() {
        bank = new Bank();
        player = new Player(PlayerColor.RED);
    }

    /**
     * Test 1 (Partition): payForSettlement works when the player has exactly the right resources
     * and at least one settlement piece.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForSettlementSucceedsWithExactResourcesAndPiece() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        player.getResources().put(ResourceType.SHEEP, 1);
        player.getResources().put(ResourceType.WHEAT, 1);
        int initialSettlements = player.getBuildings().get(BuildingType.SETTLEMENT);
        int initialVictoryPoints = player.getVictoryPoints();

        boolean result = bank.payForSettlement(player);

        assertTrue(result, "Settlement purchase should succeed with exact resources and piece");
        assertEquals(0, player.getResources().get(ResourceType.WOOD), "Wood resources should be zero after purchase");
        assertEquals(0, player.getResources().get(ResourceType.BRICK), "Brick resources should be zero after purchase");
        assertEquals(0, player.getResources().get(ResourceType.SHEEP), "Sheep resources should be zero after purchase");
        assertEquals(0, player.getResources().get(ResourceType.WHEAT), "Wheat resources should be zero after purchase");
        assertEquals(initialSettlements - 1, player.getBuildings().get(BuildingType.SETTLEMENT), "Settlement count should decrease by 1");
        assertEquals(initialVictoryPoints + 1, player.getVictoryPoints(), "Victory points should increase by 1");
    }

     /**
     * Test 2 (Boundary case): payForSettlement fails if the player lacks at least one required resource.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForSettlementFailsWhenInsufficientResources() {
        int initialSettlements = player.getBuildings().get(BuildingType.SETTLEMENT);
        int initialVictoryPoints = player.getVictoryPoints();

        boolean result = bank.payForSettlement(player);

        assertFalse(result, "Settlement purchase should fail without sufficient resources");
        assertEquals(initialSettlements, player.getBuildings().get(BuildingType.SETTLEMENT), "Settlement count should remain unchanged");
        assertEquals(initialVictoryPoints, player.getVictoryPoints(), "Victory points should remain unchanged");
    }

    /**
     * Test 3: payForCity works correctly with exact resources, reducing them to zero
     * and updating buildings and victory points.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForCitySucceedsWithExactResourcesAndPiece() {
        player.getResources().put(ResourceType.ORE, 3);
        player.getResources().put(ResourceType.WHEAT, 2);
        int initialCities = player.getBuildings().get(BuildingType.CITY);
        int initialVictoryPoints = player.getVictoryPoints();

        boolean result = bank.payForCity(player);

        assertTrue(result, "City purchase should succeed with exact resources and piece");
        assertEquals(0, player.getResources().get(ResourceType.ORE), "Ore resources should be zero after purchase");
        assertEquals(0, player.getResources().get(ResourceType.WHEAT), "Wheat resources should be zero after purchase");
        assertEquals(initialCities - 1, player.getBuildings().get(BuildingType.CITY), "City count should decrease by 1");
        assertEquals(initialVictoryPoints + 1, player.getVictoryPoints(), "Victory points should increase by 1");
    }

    /**
     * Test 4: payForRoad fails if the player has resources but no road pieces left.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForRoadFailsWhenNoRoadPiecesLeft() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        player.getBuildings().put(BuildingType.ROAD, 0);

        boolean result = bank.payForRoad(player);

        assertFalse(result, "Road purchase should fail with no road pieces left");
        assertEquals(0, player.getBuildings().get(BuildingType.ROAD), "Road count should remain zero");
    }

     /**
     * Test 5: useSettlementPieceSetup fails if the player has no settlement pieces left.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void useSettlementPieceSetupFailsWhenNoPiecesLeft() {
        player.getBuildings().put(BuildingType.SETTLEMENT, 0);

        boolean result = bank.useSettlementPieceSetup(player);

        assertFalse(result, "Settlement piece setup should fail with no pieces left");
        assertEquals(0, player.getBuildings().get(BuildingType.SETTLEMENT), "Settlement count should remain zero");
    }
}