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

        /* Buying again with no resources left should fail */
        assertFalse(bank.payForCity(player), "City purchase should fail when player has insufficient resources");
    }

    /**
     * Test 4: payForRoad succeeds with sufficient resources and pieces, consuming both.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForRoadSucceedsWithExactResourcesAndPiece() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        int initialRoads = player.getBuildings().get(BuildingType.ROAD);

        boolean result = bank.payForRoad(player);

        assertTrue(result, "Road purchase should succeed with exact resources and pieces");
        assertEquals(0, player.getResources().get(ResourceType.WOOD), "Wood resources should be zero after purchase");
        assertEquals(0, player.getResources().get(ResourceType.BRICK), "Brick resources should be zero after purchase");
        assertEquals(initialRoads - 1, player.getBuildings().get(BuildingType.ROAD), "Road count should decrease by 1");
    }

    /**
     * Test 5: useSettlementPieceSetup and useRoadPieceSetup succeed when pieces are available
     * and fail when no pieces remain.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void useSetupPiecesSucceedThenFailWhenEmpty() {
        int initialSettlements = player.getBuildings().get(BuildingType.SETTLEMENT);
        int initialRoads = player.getBuildings().get(BuildingType.ROAD);

        assertTrue(bank.useSettlementPieceSetup(player), "Setup settlement should succeed with pieces available");
        assertEquals(initialSettlements - 1, player.getBuildings().get(BuildingType.SETTLEMENT), "Settlement count should decrease by 1");

        assertTrue(bank.useRoadPieceSetup(player), "Setup road should succeed with pieces available");
        assertEquals(initialRoads - 1, player.getBuildings().get(BuildingType.ROAD), "Road count should decrease by 1");

        player.getBuildings().put(BuildingType.SETTLEMENT, 0);
        player.getBuildings().put(BuildingType.ROAD, 0);

        assertFalse(bank.useSettlementPieceSetup(player), "Setup settlement should fail with no pieces left");
        assertFalse(bank.useRoadPieceSetup(player), "Setup road should fail with no pieces left");
    }
}
