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

    /**
     * Test 6: payForRoad fails when the player has insufficient resources.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForRoadFailsWhenInsufficientResources() {
        /* Player starts with 0 WOOD and 0 BRICK (default constructor zeros all resources) */
        player.getResources().put(ResourceType.WOOD, 0);
        player.getResources().put(ResourceType.BRICK, 0);
        int initialRoads = player.getBuildings().get(BuildingType.ROAD);

        boolean result = bank.payForRoad(player);

        assertFalse(result, "Road purchase should fail without sufficient resources");
        assertEquals(initialRoads, player.getBuildings().get(BuildingType.ROAD), "Road count should remain unchanged");
    }

    /**
     * Test 7: payForSettlement fails when the player has no settlement pieces left,
     * even if they have the required resources.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForSettlementFailsWhenNoPiecesRemain() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        player.getResources().put(ResourceType.SHEEP, 1);
        player.getResources().put(ResourceType.WHEAT, 1);
        player.getBuildings().put(BuildingType.SETTLEMENT, 0);

        boolean result = bank.payForSettlement(player);

        assertFalse(result, "Settlement purchase should fail when no settlement pieces remain");
        assertEquals(1, player.getResources().get(ResourceType.WOOD), "Resources should not be deducted on failure");
    }

    /**
     * Test 8: payForCity fails when the player has no city pieces left,
     * even if they have the required resources.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void payForCityFailsWhenNoPiecesRemain() {
        player.getResources().put(ResourceType.ORE, 3);
        player.getResources().put(ResourceType.WHEAT, 2);
        player.getBuildings().put(BuildingType.CITY, 0);

        boolean result = bank.payForCity(player);

        assertFalse(result, "City purchase should fail when no city pieces remain");
        assertEquals(3, player.getResources().get(ResourceType.ORE), "Resources should not be deducted on failure");
    }

    /**
     * Test 9: refundRoad restores WOOD x1, BRICK x1, and one road piece to the player.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void refundRoadRestoresResourcesAndPiece() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        bank.payForRoad(player); /* deduct resources and piece */

        int roadsAfterPay = player.getBuildings().get(BuildingType.ROAD);
        bank.refundRoad(player);

        assertEquals(1, player.getResources().get(ResourceType.WOOD), "refundRoad should restore 1 WOOD");
        assertEquals(1, player.getResources().get(ResourceType.BRICK), "refundRoad should restore 1 BRICK");
        assertEquals(roadsAfterPay + 1, player.getBuildings().get(BuildingType.ROAD), "refundRoad should restore 1 road piece");
    }

    /**
     * Test 10: refundSettlement restores WOOD, BRICK, SHEEP, WHEAT x1 each,
     * one settlement piece, and removes one victory point.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void refundSettlementRestoresResourcesPieceAndVictoryPoint() {
        player.getResources().put(ResourceType.WOOD, 1);
        player.getResources().put(ResourceType.BRICK, 1);
        player.getResources().put(ResourceType.SHEEP, 1);
        player.getResources().put(ResourceType.WHEAT, 1);
        bank.payForSettlement(player); /* deduct resources, piece, add VP */

        int settlementsAfterPay = player.getBuildings().get(BuildingType.SETTLEMENT);
        int vpAfterPay = player.getVictoryPoints();
        bank.refundSettlement(player);

        assertEquals(1, player.getResources().get(ResourceType.WOOD), "refundSettlement should restore 1 WOOD");
        assertEquals(1, player.getResources().get(ResourceType.BRICK), "refundSettlement should restore 1 BRICK");
        assertEquals(1, player.getResources().get(ResourceType.SHEEP), "refundSettlement should restore 1 SHEEP");
        assertEquals(1, player.getResources().get(ResourceType.WHEAT), "refundSettlement should restore 1 WHEAT");
        assertEquals(settlementsAfterPay + 1, player.getBuildings().get(BuildingType.SETTLEMENT), "refundSettlement should restore 1 settlement piece");
        assertEquals(vpAfterPay - 1, player.getVictoryPoints(), "refundSettlement should remove 1 victory point");
    }

    /**
     * Test 11: refundCity restores ORE x3 and WHEAT x2, one city piece,
     * and removes one victory point.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void refundCityRestoresResourcesPieceAndVictoryPoint() {
        player.getResources().put(ResourceType.ORE, 3);
        player.getResources().put(ResourceType.WHEAT, 2);
        bank.payForCity(player); /* deduct resources, piece, add VP */

        int citiesAfterPay = player.getBuildings().get(BuildingType.CITY);
        int vpAfterPay = player.getVictoryPoints();
        bank.refundCity(player);

        assertEquals(3, player.getResources().get(ResourceType.ORE), "refundCity should restore 3 ORE");
        assertEquals(2, player.getResources().get(ResourceType.WHEAT), "refundCity should restore 2 WHEAT");
        assertEquals(citiesAfterPay + 1, player.getBuildings().get(BuildingType.CITY), "refundCity should restore 1 city piece");
        assertEquals(vpAfterPay - 1, player.getVictoryPoints(), "refundCity should remove 1 victory point");
    }

    /**
     * Test 12: static cost getters return non-null maps with the correct resource entries.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void staticCostGettersReturnCorrectMaps() {
        assertNotNull(Bank.getSettlementCost(), "getSettlementCost should return a non-null map");
        assertTrue(Bank.getSettlementCost().containsKey(ResourceType.WOOD), "Settlement cost should require WOOD");
        assertTrue(Bank.getSettlementCost().containsKey(ResourceType.BRICK), "Settlement cost should require BRICK");
        assertTrue(Bank.getSettlementCost().containsKey(ResourceType.SHEEP), "Settlement cost should require SHEEP");
        assertTrue(Bank.getSettlementCost().containsKey(ResourceType.WHEAT), "Settlement cost should require WHEAT");

        assertNotNull(Bank.getCityCost(), "getCityCost should return a non-null map");
        assertTrue(Bank.getCityCost().containsKey(ResourceType.ORE), "City cost should require ORE");
        assertTrue(Bank.getCityCost().containsKey(ResourceType.WHEAT), "City cost should require WHEAT");

        assertNotNull(Bank.getRoadCost(), "getRoadCost should return a non-null map");
        assertTrue(Bank.getRoadCost().containsKey(ResourceType.WOOD), "Road cost should require WOOD");
        assertTrue(Bank.getRoadCost().containsKey(ResourceType.BRICK), "Road cost should require BRICK");
    }
}
