package SettlersOfCatan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the console input from a human player into moves on the board.
 *
 * Supported syntax (case-insensitive, leading/trailing whitespace ignored):
 *   roll
 *   go
 *   list
 *   build settlement <nodeId>
 *   build city <nodeId>
 *   build road <fromNodeId>, <toNodeId>
 *
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public final class HumanCommandParser {

    // Patterns are compiled once at class load — not on every parse() call
    private static final Pattern ROLL = Pattern.compile("(?i)roll");
    private static final Pattern GO = Pattern.compile("(?i)go");
    private static final Pattern LIST = Pattern.compile("(?i)list");
    private static final Pattern BUILD_SETTLEMENT = Pattern.compile("(?i)build\\s+settlement\\s+(\\d+)");
    private static final Pattern BUILD_CITY = Pattern.compile("(?i)build\\s+city\\s+(\\d+)");
    private static final Pattern BUILD_ROAD = Pattern.compile("(?i)build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)");

    // Prevent instantiation 
    private HumanCommandParser() {}


    /**
     * Parses a line of console input into a ParsedCommand.
     * Returns a command with action UNKNOWN if the input matches nothing.
     *
     * @param line raw input string (may be null)
     * @return parsed command, never null
     */
    public static ParsedCommand parse(String line) {
        if (line == null || line.isBlank()) {
            return ParsedCommand.unknown();
        }

        String input = line.trim();

        if (ROLL.matcher(input).matches()){
            return ParsedCommand.of(Action.ROLL);
        }
        if (GO.matcher(input).matches()){
            return ParsedCommand.of(Action.GO);
        }
        if (LIST.matcher(input).matches()){
            return ParsedCommand.of(Action.LIST);
        }

        Matcher m;

        m = BUILD_SETTLEMENT.matcher(input);
        if (m.matches()){
            return ParsedCommand.buildNode(Action.BUILD_SETTLEMENT, Integer.parseInt(m.group(1)));
        }

        m = BUILD_CITY.matcher(input);
        if (m.matches()){
            return ParsedCommand.buildNode(Action.BUILD_CITY, Integer.parseInt(m.group(1)));
        }

        m = BUILD_ROAD.matcher(input);
        if (m.matches()){
            return ParsedCommand.buildRoad(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }

        return ParsedCommand.unknown();
    }


    public enum Action {
        ROLL, GO, LIST, BUILD_SETTLEMENT, BUILD_CITY, BUILD_ROAD, UNKNOWN
    }


    /**
     * Immutable result of parsing one line of input.
     * Use factory methods (of, buildNode, buildRoad, unknown) to construct.
     */
    public static final class ParsedCommand {
        private final Action action;
        private final int nodeId;      // for settlement / city (-1 if unused)
        private final int fromNodeId;  // for road (-1 if unused)
        private final int toNodeId;    // for road (-1 if unused)

        private ParsedCommand(Action action, int nodeId, int fromNodeId, int toNodeId) {
            this.action     = action;
            this.nodeId     = nodeId;
            this.fromNodeId = fromNodeId;
            this.toNodeId   = toNodeId;
        }

        // Factory methods keep construction readable at call sites
        static ParsedCommand of(Action action) { 
            return new ParsedCommand(action, -1, -1, -1);
        }
        static ParsedCommand buildNode(Action action, int nodeId) { 
            return new ParsedCommand(action, nodeId, -1, -1);
        }
        static ParsedCommand buildRoad(int from, int to) { 
            return new ParsedCommand(Action.BUILD_ROAD, -1, from, to);
        }
        static ParsedCommand unknown() { 
            return new ParsedCommand(Action.UNKNOWN, -1, -1, -1);
        }

        public Action getAction() { 
            return action;
        }
        public int getNodeId() {
            return nodeId;
        }
        public int getFromNodeId() {
            return fromNodeId;
        }
        public int getToNodeId() {
            return toNodeId;
        }

        @Override
        public String toString() {
            return switch (action) {
                case BUILD_SETTLEMENT, BUILD_CITY -> action + " @ node " + nodeId;
                case BUILD_ROAD -> action + " from " + fromNodeId + " to " + toNodeId;
                default -> action.toString();
            };
        }
    }
}
