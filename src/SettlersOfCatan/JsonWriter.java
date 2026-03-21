package SettlersOfCatan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes game state to JSON for the visualizer (R2.3).
 * Uses plain file I/O to produce the format expected by light_visualizer.py.
 *
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class JsonWriter {

	/**
	 * Writes the board layout (tiles) to a JSON file in the format expected by
	 * light_visualizer.py as base_map.json (q, s, r, resource, number per tile).
	 *
	 * @param board the game board
	 * @param path  path to the output file (e.g. "src/SettlersOfCatan/visualize/base_map.json")
	 * @throws IOException if the file cannot be written
	 */
	public static void writeBaseMap(Board board, String path) throws IOException {
		String json = buildBaseMapJson(board);
		Files.writeString(Path.of(path), json);
	}

	/**
	 * Writes the current board state to a JSON file in the format expected by
	 * light_visualizer.py (roads and buildings).
	 *
	 * @param board the game board
	 * @param path  path to the output file (e.g. "src/SettlersOfCatan/visualize/state.json")
	 * @throws IOException if the file cannot be written
	 */
	public static void writeState(Board board, String path) throws IOException {
		String json = buildStateJson(board);
		Files.writeString(Path.of(path), json);
	}

	/**
	 * Builds the JSON document for the static board layout (tiles).
	 *
	 * @param board the game board
	 * @return JSON string for base_map.json
	 */
	private static String buildBaseMapJson(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"tiles\": ");
		appendTiles(board, sb);
		sb.append("\n}\n");
		return sb.toString();
	}

	/**
	 * Appends the tiles array to the given buffer.
	 *
	 * @param board the game board
	 * @param sb    target buffer
	 */
	private static void appendTiles(Board board, StringBuilder sb) {
		sb.append("[\n");
		Tile[] tiles = board.getTiles();
		boolean first = true;
		if (tiles != null) {
			for (Tile tile : tiles) {
				if (tile == null) {
					continue;
				}
				if (!first) {
					sb.append(",\n");
				}
				first = false;
				sb.append("    ");
				ResourceType res = tile.produceResource();
				boolean desert = (res == null || res == ResourceType.NULL);
				sb.append("{ \"q\": ").append(tile.getQ());
				sb.append(", \"s\": ").append(tile.getS());
				sb.append(", \"r\": ").append(tile.getR());
				if (desert) {
					sb.append(", \"resource\": null, \"number\": null");
				} else {
					sb.append(", \"resource\": \"").append(res.name()).append("\"");
					sb.append(", \"number\": ").append(tile.getNumber());
				}
				sb.append(" }");
			}
		}
		sb.append("\n  ]");
	}

	/**
	 * Builds the JSON document for the dynamic game state (roads and buildings).
	 *
	 * @param board the game board
	 * @return JSON string for state.json
	 */
	private static String buildStateJson(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"roads\": ");
		appendRoads(board, sb);
		sb.append(",\n");
		sb.append("  \"buildings\": ");
		appendBuildings(board.getNodes(), sb);
		sb.append("\n}\n");
		return sb.toString();
	}

	/**
	 * Appends the roads array (edges with roads) to the given buffer.
	 *
	 * @param board the game board
	 * @param sb    target buffer
	 */
	private static void appendRoads(Board board, StringBuilder sb) {
		sb.append("[\n");
		Edge[] edges = board.getEdges();
		boolean first = true;
		if (edges != null) {
			for (Edge edge : edges) {
				if (edge == null || edge.getRoad() == null) {
					continue;
				}
				if (!first) {
					sb.append(",\n");
				}
				first = false;
				sb.append("    ");
				String owner = edge.getRoad().getOwner().getColor().name();
				sb.append("{ \"a\": ").append(edge.getNodeA().getId());
				sb.append(", \"b\": ").append(edge.getNodeB().getId());
				sb.append(", \"owner\": \"").append(owner).append("\" }");
			}
		}
		sb.append("\n  ]");
	}

	/**
	 * Appends the buildings array for occupied nodes to the given buffer.
	 *
	 * @param nodes intersection nodes (may contain null entries)
	 * @param sb    target buffer
	 */
	private static void appendBuildings(Node[] nodes, StringBuilder sb) {
		sb.append("[\n");
		boolean first = true;
		if (nodes != null) {
			for (Node node : nodes) {
				if (node == null || node.getBuilding() == null) {
					continue;
				}
				if (!first) {
					sb.append(",\n");
				}
				first = false;
				sb.append("    ");
				String owner = node.getOccupyingPlayer().getColor().name();
				String type = node.getBuilding() instanceof City ? "CITY" : "SETTLEMENT";
				sb.append("{ \"node\": ").append(node.getId());
				sb.append(", \"owner\": \"").append(owner).append("\"");
				sb.append(", \"type\": \"").append(type).append("\" }");
			}
		}
		sb.append("\n  ]");
	}
}
