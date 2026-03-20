/**
 * Unit tests for Assignment 3 features: Command Pattern and Chain of Responsibility.
 * Covers CommandHistory undo/redo state machine, the three build command objects,
 * and the AI handler chain delegation logic.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */

package UnitTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import SettlersOfCatan.ActionHandler;
import SettlersOfCatan.Bank;
import SettlersOfCatan.BuildCityCommand;
import SettlersOfCatan.BuildRoadCommand;
import SettlersOfCatan.BuildSettlementCommand;
import SettlersOfCatan.City;
import SettlersOfCatan.CommandHistory;
import SettlersOfCatan.ConnectRoadsHandler;
import SettlersOfCatan.DefendRoadHandler;
import SettlersOfCatan.Edge;
import SettlersOfCatan.GameCommand;
import SettlersOfCatan.Node;
import SettlersOfCatan.OverHandSizeHandler;
import SettlersOfCatan.Player;
import SettlersOfCatan.PlayerActions;
import SettlersOfCatan.PlayerColor;
import SettlersOfCatan.ResourceType;
import SettlersOfCatan.Road;
import SettlersOfCatan.Settlement;
import SettlersOfCatan.ValueScoringHandler;

/**
 * Tests for the Command Pattern and Chain of Responsibility patterns introduced in Assignment 3.
 */
public class Assignment3Test {

    /** Default test timeout in seconds */
    private static final int DEFAULT_TIMEOUT = 2;

    /** Player instance used across tests */
    private Player player;

    /** Bank instance used across tests */
    private Bank bank;

    /**
     * Resets player (with all resources zeroed) and bank before each test.
     */
    @BeforeEach
    public void setUp() {
        player = new Player(PlayerColor.RED);
        bank = new Bank();
        // Zero out all resources for clean, predictable tests
        for (ResourceType rt : ResourceType.values()) {
            player.getResources().put(rt, 0);
        }
    }

    // =========================================================================
    // Stub helpers
    // =========================================================================

    /**
     * Stub PlayerActions that avoids real board/validator/bank dependencies.
     * Subclasses may override placeCity to control city placement behaviour.
     */
    private static class StubPlayerActions extends PlayerActions {
        private final List<String> actionsToReturn;
        boolean buildRoadCalled;
        boolean buildSettlementCalled;
        boolean buildCityCalled;

        StubPlayerActions(List<String> actionsToReturn) {
            super(null, null, null, null, new Random(0));
            this.actionsToReturn = actionsToReturn;
        }

        @Override
        public List<String> getAvailableActions(Player p, boolean forced) {
            return new ArrayList<>(actionsToReturn);
        }

        @Override public void buildRoad(Player p, int r)       { buildRoadCalled = true; }
        @Override public void buildSettlement(Player p, int r) { buildSettlementCalled = true; }
        @Override public void buildCity(Player p, int r)       { buildCityCalled = true; }

        /** No-op: prevents file I/O during unit tests. */
        @Override void refreshVisualizerState() {}
    }

    /**
     * Simple ActionHandler that records whether handleTurn was called.
     * Used as a terminal sentinel in chain-of-responsibility tests.
     */
    private static class TrackingHandler extends ActionHandler {
        boolean wasCalled;
        TrackingHandler() { super(new Random()); }

        @Override
        public void handleTurn(Player p, PlayerActions a, int r) {
            wasCalled = true;
        }
    }

    // =========================================================================
    // CommandHistory – state machine tests
    // =========================================================================

    /**
     * Test 1: pushToStack must execute the command immediately.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void pushToStackExecutesCommandImmediately() {
        boolean[] executed = {false};
        GameCommand cmd = new GameCommand() {
            public void execute() { executed[0] = true; }
            public void undo()    {}
        };

        CommandHistory history = new CommandHistory();
        history.pushToStack(cmd);

        assertTrue(executed[0], "pushToStack should execute the command");
    }

    /**
     * Test 2: undo calls undo() on the last command, moves it to the redo stack,
     * and leaves the history stack empty.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void undoCallsUndoOnLastCommandAndEnablesRedo() {
        boolean[] undone = {false};
        GameCommand cmd = new GameCommand() {
            public void execute() {}
            public void undo()    { undone[0] = true; }
        };

        CommandHistory history = new CommandHistory();
        history.pushToStack(cmd);

        assertFalse(history.canRedo(), "canRedo should be false before any undo");
        history.undo();

        assertTrue(undone[0],           "undo should invoke the command's undo() method");
        assertTrue(history.canRedo(),   "canRedo should be true after undo");
        assertFalse(history.canUndo(),  "canUndo should be false after undoing the only command");
    }

    /**
     * Test 3: redo re-executes the most recently undone command and moves it
     * back to the history stack.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void redoReExecutesPreviouslyUndoneCommand() {
        int[] counter = {0};
        GameCommand cmd = new GameCommand() {
            public void execute() { counter[0]++; }
            public void undo()    { counter[0]--; }
        };

        CommandHistory history = new CommandHistory();
        history.pushToStack(cmd); // counter = 1
        history.undo();           // counter = 0
        history.redo();           // counter = 1

        assertEquals(1, counter[0],    "redo should re-execute the command");
        assertFalse(history.canRedo(), "canRedo should be false after consuming the redo entry");
        assertTrue(history.canUndo(),  "canUndo should be true after redo");
    }

    /**
     * Test 4: pushing a new command after an undo must clear the redo stack,
     * preventing branching history.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void newPushAfterUndoClearsRedoStack() {
        GameCommand dummy = new GameCommand() {
            public void execute() {}
            public void undo()    {}
        };

        CommandHistory history = new CommandHistory();
        history.pushToStack(dummy);
        history.undo();
        assertTrue(history.canRedo(), "canRedo should be true after undo");

        history.pushToStack(dummy); // new command should wipe redo stack
        assertFalse(history.canRedo(),
            "pushing a new command after undo should clear the redo stack");
    }

    /**
     * Test 5: canUndo and canRedo correctly reflect the state of both stacks.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void canUndoAndCanRedoReflectStackState() {
        CommandHistory history = new CommandHistory();
        assertFalse(history.canUndo(), "canUndo should be false on an empty history");
        assertFalse(history.canRedo(), "canRedo should be false on an empty redo stack");

        GameCommand dummy = new GameCommand() {
            public void execute() {}
            public void undo()    {}
        };
        history.pushToStack(dummy);
        assertTrue(history.canUndo(),  "canUndo should be true after a push");
        assertFalse(history.canRedo(), "canRedo should still be false");
    }

    // =========================================================================
    // BuildRoadCommand
    // =========================================================================

    /**
     * Test 6: BuildRoadCommand.undo() must remove the road from the edge
     * and refund the road cost (WOOD x1, BRICK x1) to the player.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void buildRoadCommandUndoRemovesRoadAndRefundsResources() {
        Node nodeA = new Node(0);
        Node nodeB = new Node(1);
        Edge edge   = new Edge(0, nodeA, nodeB);
        StubPlayerActions actions = new StubPlayerActions(new ArrayList<>());

        // Simulate state after execute(): road placed, resources deducted
        edge.setRoad(new Road(player, edge));
        player.getResources().put(ResourceType.WOOD,  0);
        player.getResources().put(ResourceType.BRICK, 0);

        BuildRoadCommand cmd = new BuildRoadCommand(edge, player, bank, actions);
        cmd.undo();

        assertNull(edge.getRoad(),
            "undo should remove the road from the edge");
        assertEquals(1, player.getResources().get(ResourceType.WOOD),
            "undo should refund 1 WOOD to the player");
        assertEquals(1, player.getResources().get(ResourceType.BRICK),
            "undo should refund 1 BRICK to the player");
    }

    // =========================================================================
    // BuildSettlementCommand
    // =========================================================================

    /**
     * Test 7: BuildSettlementCommand.undo() must clear the building and occupying
     * player from the node, refund settlement resources, and remove the victory point.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void buildSettlementCommandUndoClearsNodeAndRefundsResources() {
        Node node = new Node(0);
        StubPlayerActions actions = new StubPlayerActions(new ArrayList<>());

        // Simulate state after execute(): settlement placed, resources deducted, VP awarded
        node.setBuilding(new Settlement(player));
        node.setOccupyingPlayer(player);
        player.getResources().put(ResourceType.WOOD,  0);
        player.getResources().put(ResourceType.BRICK, 0);
        player.getResources().put(ResourceType.SHEEP, 0);
        player.getResources().put(ResourceType.WHEAT, 0);
        player.addVictoryPoint(1);

        BuildSettlementCommand cmd = new BuildSettlementCommand(node, player, bank, actions);
        cmd.undo();

        assertNull(node.getBuilding(),
            "undo should clear the building from the node");
        assertNull(node.getOccupyingPlayer(),
            "undo should clear the occupying player from the node");
        assertEquals(1, player.getResources().get(ResourceType.WOOD),
            "undo should refund 1 WOOD");
        assertEquals(1, player.getResources().get(ResourceType.BRICK),
            "undo should refund 1 BRICK");
        assertEquals(1, player.getResources().get(ResourceType.SHEEP),
            "undo should refund 1 SHEEP");
        assertEquals(1, player.getResources().get(ResourceType.WHEAT),
            "undo should refund 1 WHEAT");
        assertEquals(0, player.getVictoryPoints(),
            "undo should remove the victory point awarded for the settlement");
    }

    // =========================================================================
    // BuildCityCommand
    // =========================================================================

    /**
     * Test 8: BuildCityCommand.execute() must capture the previous settlement,
     * and undo() must restore it along with the city cost refund.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void buildCityCommandUndoRestoresPreviousSettlement() {
        Node node = new Node(0);
        Settlement previousSettlement = new Settlement(player);
        node.setBuilding(previousSettlement);
        node.setOccupyingPlayer(player);

        // Stub that actually places the City during execute() so previousBuilding is captured
        StubPlayerActions actions = new StubPlayerActions(new ArrayList<>()) {
            @Override
            public boolean placeCity(Node n, Player p) {
                n.setBuilding(new City(p));
                n.setOccupyingPlayer(p);
                return true;
            }
        };

        // Simulate resources deducted for city (undo will refund them)
        player.getResources().put(ResourceType.ORE,   0);
        player.getResources().put(ResourceType.WHEAT, 0);
        player.addVictoryPoint(1); // VP from earlier settlement

        BuildCityCommand cmd = new BuildCityCommand(node, player, bank, actions);
        cmd.execute();

        assertTrue(node.getBuilding() instanceof City,
            "execute should upgrade the node to a City");

        cmd.undo();

        assertSame(previousSettlement, node.getBuilding(),
            "undo should restore the exact previous settlement object");
        assertEquals(3, player.getResources().get(ResourceType.ORE),
            "undo should refund 3 ORE");
        assertEquals(2, player.getResources().get(ResourceType.WHEAT),
            "undo should refund 2 WHEAT");
    }

    // =========================================================================
    // Chain of Responsibility – OverHandSizeHandler
    // =========================================================================

    /**
     * Test 9: OverHandSizeHandler must delegate to its successor when the
     * player holds 7 or fewer real cards.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void overHandSizeHandlerDelegatesToSuccessorWhenHandSmall() {
        // All resources zeroed in setUp — 0 real cards <= 7
        TrackingHandler successor = new TrackingHandler();
        OverHandSizeHandler handler = new OverHandSizeHandler(new Random());
        handler.setSuccessor(successor);

        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("SETTLEMENT", "PASS"));
        handler.handleTurn(player, actions, 1);

        assertTrue(successor.wasCalled,
            "OverHandSizeHandler should delegate to successor when hand size is <= 7");
    }

    /**
     * Test 10: OverHandSizeHandler must act (not delegate) when the player
     * holds more than 7 real cards and a non-PASS action is available.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void overHandSizeHandlerActsAndDoesNotDelegateWhenHandLarge() {
        // Give player 8 WOOD — clearly over the 7-card threshold
        player.getResources().put(ResourceType.WOOD, 8);

        TrackingHandler successor = new TrackingHandler();
        OverHandSizeHandler handler = new OverHandSizeHandler(new Random(0));
        handler.setSuccessor(successor);

        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("SETTLEMENT"));
        handler.handleTurn(player, actions, 1);

        assertTrue(actions.buildSettlementCalled,
            "OverHandSizeHandler should build to reduce hand size when over 7 cards");
        assertFalse(successor.wasCalled,
            "OverHandSizeHandler should NOT delegate when it handles the turn itself");
    }

    // =========================================================================
    // Chain of Responsibility – ValueScoringHandler
    // =========================================================================

    /**
     * Test 11: ValueScoringHandler is the terminal handler — it must not
     * require a successor and must not throw when none is set.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void valueScoringHandlerIsTerminalAndDoesNotCrash() {
        ValueScoringHandler handler = new ValueScoringHandler(new Random(0));
        // No successor set on purpose
        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("PASS"));

        assertDoesNotThrow(() -> handler.handleTurn(player, actions, 1),
            "ValueScoringHandler must handle a turn without throwing even when no successor is set");
    }

    /**
     * Test 12: ValueScoringHandler must prefer SETTLEMENT/CITY (score 1.0) over
     * ROAD (score <= 0.8) when both are available.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void valueScoringHandlerPrefersSettlementOverRoad() {
        ValueScoringHandler handler = new ValueScoringHandler(new Random(0));
        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("ROAD", "SETTLEMENT"));

        handler.handleTurn(player, actions, 1);

        assertTrue(actions.buildSettlementCalled,
            "ValueScoringHandler should choose SETTLEMENT (score 1.0) over ROAD");
        assertFalse(actions.buildRoadCalled,
            "ValueScoringHandler should not build a road when a higher-scoring action exists");
    }

    // =========================================================================
    // Chain of Responsibility – full chain delegation
    // =========================================================================

    /**
     * Test 13: When no handler's condition is triggered, the request must
     * propagate all the way to the terminal handler.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void chainPropagatesRequestToTerminalWhenNoConditionsMet() {
        // 0 real cards — OverHandSizeHandler will not act
        TrackingHandler terminal = new TrackingHandler();
        OverHandSizeHandler handler = new OverHandSizeHandler(new Random());
        handler.setSuccessor(terminal);

        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("PASS"));
        handler.handleTurn(player, actions, 1);

        assertTrue(terminal.wasCalled,
            "The request should reach the terminal handler when no earlier handler acts");
    }

    /**
     * Test 14: setSuccessor correctly links handlers — a three-handler chain
     * skips the first two and reaches the terminal when neither condition fires.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void setSuccessorLinksMultipleHandlersIntoChain() {
        // 0 real cards — OverHandSizeHandler delegates
        // DefendRoadHandler needs Board to compute road lengths; delegate via a passthrough stub
        TrackingHandler terminal = new TrackingHandler();

        // Use two OverHandSizeHandlers as stand-ins for a multi-link chain
        OverHandSizeHandler first  = new OverHandSizeHandler(new Random());
        OverHandSizeHandler second = new OverHandSizeHandler(new Random());
        first.setSuccessor(second);
        second.setSuccessor(terminal);

        StubPlayerActions actions = new StubPlayerActions(Arrays.asList("PASS"));
        first.handleTurn(player, actions, 1);

        assertTrue(terminal.wasCalled,
            "A three-handler chain should propagate the request to the terminal");
    }
}
