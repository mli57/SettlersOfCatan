/**
 * Tests for the HumanCommandParser class in our Settlers of Catan game.
 * Verifies that console input is correctly parsed into game commands.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */

package UnitTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import SettlersOfCatan.HumanCommandParser;
import SettlersOfCatan.HumanCommandParser.Action;
import SettlersOfCatan.HumanCommandParser.ParsedCommand;

/**
 * Unit tests for the HumanCommandParser class.
 * Covers valid commands, case insensitivity, whitespace handling, and unknown input.
 */
public class HumanCommandParserTest {

    /* Default timeout variable time */
    private static final int DEFAULT_TIMEOUT = 2;

    /**
     * Test 1 (Partition): "roll" parses to Action.ROLL with no node IDs set.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseRollReturnsRollAction() {
        ParsedCommand cmd = HumanCommandParser.parse("roll");

        assertEquals(Action.ROLL, cmd.getAction(), "\"roll\" should parse to ROLL");
        assertEquals(-1, cmd.getNodeId(), "ROLL should have no nodeId");
        assertEquals(-1, cmd.getFromNodeId(), "ROLL should have no fromNodeId");
        assertEquals(-1, cmd.getToNodeId(), "ROLL should have no toNodeId");
    }

    /**
     * Test 2 (Partition): "go" parses to Action.GO with no node IDs set.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseGoReturnsGoAction() {
        ParsedCommand cmd = HumanCommandParser.parse("go");

        assertEquals(Action.GO, cmd.getAction(), "\"go\" should parse to GO");
        assertEquals(-1, cmd.getNodeId(), "GO should have no nodeId");
    }

    /**
     * Test 3 (Partition): "list" parses to Action.LIST.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseListReturnsListAction() {
        ParsedCommand cmd = HumanCommandParser.parse("list");

        assertEquals(Action.LIST, cmd.getAction(), "\"list\" should parse to LIST");
    }

    /**
     * Test 4 (Partition): "build settlement <nodeId>" parses to Action.BUILD_SETTLEMENT
     * and captures the correct node ID.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseBuildSettlementReturnsCorrectNodeId() {
        ParsedCommand cmd = HumanCommandParser.parse("build settlement 42");

        assertEquals(Action.BUILD_SETTLEMENT, cmd.getAction(), "Should parse to BUILD_SETTLEMENT");
        assertEquals(42, cmd.getNodeId(), "Node ID should be 42");
        assertEquals(-1, cmd.getFromNodeId(), "fromNodeId should be unused");
        assertEquals(-1, cmd.getToNodeId(), "toNodeId should be unused");
    }

    /**
     * Test 5 (Partition): "build city <nodeId>" parses to Action.BUILD_CITY
     * and captures the correct node ID.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseBuildCityReturnsCorrectNodeId() {
        ParsedCommand cmd = HumanCommandParser.parse("build city 7");

        assertEquals(Action.BUILD_CITY, cmd.getAction(), "Should parse to BUILD_CITY");
        assertEquals(7, cmd.getNodeId(), "Node ID should be 7");
        assertEquals(-1, cmd.getFromNodeId(), "fromNodeId should be unused");
        assertEquals(-1, cmd.getToNodeId(), "toNodeId should be unused");
    }

    /**
     * Test 6 (Partition): "build road <from>, <to>" parses to Action.BUILD_ROAD
     * and captures both node IDs.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseBuildRoadReturnsCorrectNodeIds() {
        ParsedCommand cmd = HumanCommandParser.parse("build road 3, 5");

        assertEquals(Action.BUILD_ROAD, cmd.getAction(), "Should parse to BUILD_ROAD");
        assertEquals(3, cmd.getFromNodeId(), "fromNodeId should be 3");
        assertEquals(5, cmd.getToNodeId(), "toNodeId should be 5");
        assertEquals(-1, cmd.getNodeId(), "nodeId should be unused for roads");
    }

    /**
     * Test 7 (Case insensitivity): Commands should parse correctly regardless of letter case.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parserIsCaseInsensitive() {
        assertEquals(Action.ROLL, HumanCommandParser.parse("ROLL").getAction(), "ROLL in uppercase should parse");
        assertEquals(Action.GO, HumanCommandParser.parse("GO").getAction(), "GO in uppercase should parse");
        assertEquals(Action.LIST, HumanCommandParser.parse("LIST").getAction(), "LIST in uppercase should parse");
        assertEquals(Action.BUILD_SETTLEMENT, HumanCommandParser.parse("BUILD SETTLEMENT 1").getAction(), "Mixed case settlement should parse");
        assertEquals(Action.BUILD_CITY, HumanCommandParser.parse("Build City 2").getAction(), "Mixed case city should parse");
        assertEquals(Action.BUILD_ROAD, HumanCommandParser.parse("BUILD ROAD 1, 2").getAction(), "Uppercase road should parse");
    }

    /**
     * Test 8 (Boundary): Leading and trailing whitespace should be ignored.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parserTrimsLeadingAndTrailingWhitespace() {
        assertEquals(Action.ROLL, HumanCommandParser.parse("  roll  ").getAction(), "Surrounding spaces should be trimmed");
        assertEquals(Action.GO, HumanCommandParser.parse("\tgo\t").getAction(), "Surrounding tabs should be trimmed");

        ParsedCommand cmd = HumanCommandParser.parse("  build settlement 10  ");
        assertEquals(Action.BUILD_SETTLEMENT, cmd.getAction(), "Surrounding spaces with build command should be trimmed");
        assertEquals(10, cmd.getNodeId(), "Node ID should still be parsed correctly after trimming");
    }

    /**
     * Test 9 (Boundary): null and blank input should return Action.UNKNOWN.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseNullAndBlankInputReturnsUnknown() {
        assertEquals(Action.UNKNOWN, HumanCommandParser.parse(null).getAction(), "null input should return UNKNOWN");
        assertEquals(Action.UNKNOWN, HumanCommandParser.parse("").getAction(), "empty string should return UNKNOWN");
        assertEquals(Action.UNKNOWN, HumanCommandParser.parse("   ").getAction(), "blank string should return UNKNOWN");
    }

    /**
     * Test 10 (Boundary): Unrecognized input should return Action.UNKNOWN with all IDs as -1.
     */
    @Test
    @Timeout(value = DEFAULT_TIMEOUT, unit = TimeUnit.SECONDS)
    public void parseGarbageInputReturnsUnknown() {
        ParsedCommand cmd = HumanCommandParser.parse("gibberish command xyz");

        assertEquals(Action.UNKNOWN, cmd.getAction(), "Unrecognized input should return UNKNOWN");
        assertEquals(-1, cmd.getNodeId(), "nodeId should be -1 for UNKNOWN");
        assertEquals(-1, cmd.getFromNodeId(), "fromNodeId should be -1 for UNKNOWN");
        assertEquals(-1, cmd.getToNodeId(), "toNodeId should be -1 for UNKNOWN");
    }
}
