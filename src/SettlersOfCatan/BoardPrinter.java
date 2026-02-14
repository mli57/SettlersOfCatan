package SettlersOfCatan;

/**
 * Utility class for printing board state.
 * GRASP: Pure Fabrication - printing is not the board's responsibility.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class BoardPrinter {

	/**
	 * Prints all tiles with their number, location (q,s,r), terrain type, and token value.
	 * @param board The board containing the tiles to print
	 */
	public static void printTiles(Board board) {
		Tile[] tiles = board.getTiles();
		if (tiles == null) {
			System.out.println("No tiles generated yet.");
			return;
		}

		System.out.println("\n=== TILES ===");
		System.out.println("Tile# | Location (q,s,r) | Type      | Token");
		System.out.println("------|-------------------|-----------|------");

		// Print each tile's information
		for (int i = 0; i < tiles.length; i++) {
			Tile tile = tiles[i];
			if (tile != null) {
				// Format token string (DESERT for 0, otherwise the number)
				String tokenStr = (tile.getNumber() == 0) ? "DESERT" : String.valueOf(tile.getNumber());
				System.out.printf("%-5d | (%2d,%2d,%2d)          | %-9s | %s%n",
					i, tile.getQ(), tile.getS(), tile.getR(),
					tile.getTerrain().toString(), tokenStr);
			}
		}
		System.out.println();
	}

	/**
	 * Prints all nodes in order (0-53).
	 * @param board The board containing the nodes to print
	 */
	public static void printNodes(Board board) {
		Node[] nodes = board.getNodes();
		if (nodes == null) {
			System.out.println("No nodes generated yet.");
			return;
		}

		System.out.println("\n=== NODES ===");
		System.out.print("Node IDs: ");
		// Print all node IDs, 10 per line for readability
		for (int i = 0; i < nodes.length; i++) {
			System.out.print(i);
			if (i < nodes.length - 1) {
				System.out.print(", ");
			}
			// Print 10 per line for readability
			if ((i + 1) % 10 == 0 && i < nodes.length - 1) {
				System.out.println();
				System.out.print("          ");
			}
		}
		System.out.println("\n");
	}
}

