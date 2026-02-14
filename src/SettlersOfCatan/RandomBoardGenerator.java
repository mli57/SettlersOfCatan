package SettlersOfCatan;

import java.util.Random;

/**
 * Generates a random Catan board with terrain and token distribution.
 * SOLID: Single Responsibility - only handles board generation.
 * GRASP: Pure Fabrication - creates board structure.
 */
public class RandomBoardGenerator implements IBoardGenerator {
	private final Random random;

	public RandomBoardGenerator() {
		this.random = new Random();
	}

	public RandomBoardGenerator(long seed) {
		this.random = new Random(seed);
	}

	@Override
	public void generate(Board board) {
		// Terrain distribution: Forest(4), Pasture(4), Fields(4), Hills(3), Mountains(3), Desert(1)
		int[] maxTerrainCount = {4, 4, 4, 3, 3, 1};
		int[] terrainCount = {0, 0, 0, 0, 0, 0};

		// Token distribution: 2(1), 3(2), 4(2), 5(2), 6(2), 8(2), 9(2), 10(2), 11(2), 12(1)
		int[] maxTokenCount = {0, 1, 2, 2, 2, 2, 0, 2, 2, 2, 2, 1}; // indices 0-11 represent numbers 1-12
		int[] tokenCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		// Counters initialized

		Tile[] tiles = new Tile[19];

		// Create all nodes up front (54 intersections)
		Node[] nodes = new Node[54];
		for (int i = 0; i < 54; i++) {
			nodes[i] = new Node(i);
		}


		int[][] tileNodes = {
			{0, 1, 2, 3, 4, 5},    // Tile  0
			{6, 7, 8, 9, 2, 1},    // Tile  1
			{2, 9, 10, 11, 12, 3},  // Tile  2
			{4, 3, 12, 13, 14, 15}, // Tile  3
			{16, 5, 4, 15, 17, 18}, // Tile  4
			{19, 20, 0, 5, 16, 21},  // Tile  5
			{22, 23, 6, 1, 0, 20},   // Tile  6
			{24, 25, 26, 27, 8, 7},  // Tile  7
			{8, 27, 28, 29, 10, 9},  // Tile  8
			{10, 29, 30, 31, 32, 11}, // Tile  9
			{12, 11, 32, 33, 34, 13}, // Tile 10
			{14, 13, 34, 35, 36, 37}, // Tile 11
			{17, 15, 14, 37, 38, 39}, // Tile 12
			{40, 18, 17, 39, 41, 42}, // Tile 13
			{43, 21, 16, 18, 40, 44}, // Tile 14
			{45, 46, 19, 21, 43, 47}, // Tile 15
			{48, 49, 22, 20, 19, 46}, // Tile 16
			{50, 51, 52, 23, 22, 49}, // Tile 17
			{52, 53, 24, 7, 6, 23}    // Tile 18
		};

		int[][] boardCoords = {
			{0, 0, 0}, {0, 1, -1}, {-1, 1, 0},
			{-1, 0, 1}, {0, -1, 1}, {1, -1, 0},
			{1, 0, -1}, {0, 2, -2}, {-1, 2, -1},
			{-2, 2, 0}, {-2, 1, 1}, {-2, 0, 2},
			{-1, -1, 2}, {0, -2, 2}, {1, -2, 1},
			{2, -2, 0}, {2, -1, -1}, {2, 0, -2},
			{1, 1, -2}
		};

		int tileNum = 0;
		// Generate all 19 tiles
		while (tileNum < 19) {
			// Select random terrain type
			int randomTerrain = random.nextInt(6);

			// Check if this terrain type is still available
			if (terrainCount[randomTerrain] < maxTerrainCount[randomTerrain]) {
				terrainCount[randomTerrain]++;
				TerrainType terrain = TerrainType.values()[randomTerrain];
				int tokenNumber = 0;

				// Assign token number if not desert
				if (terrain != TerrainType.DESERT) {
					int randomToken;
					do {
						randomToken = random.nextInt(12) + 1; // numbers 1-12
					} while (tokenCount[randomToken - 1] >= maxTokenCount[randomToken - 1]);

					tokenCount[randomToken - 1]++;
					tokenNumber = randomToken;
				}

				tiles[tileNum] = new Tile(
					boardCoords[tileNum][0],
					boardCoords[tileNum][1],
					boardCoords[tileNum][2],
					terrain,
					tokenNumber,
					tileNodes[tileNum]
				);

				tileNum++;
			}
		}


		// Set tiles and nodes on board
		board.setTiles(tiles);
		board.setNodes(nodes);
		// Board structure populated

		// Generate all edges between nodes
		generateEdges(board, tiles, nodes);
		// Board generation complete
	}

	/**
	 * Generates all edges by going through each tile and creating edges between consecutive nodes.
	 */
	private void generateEdges(Board board, Tile[] tiles, Node[] nodes) {
		// Catan board has exactly 72 unique edges
		Edge[] edges = new Edge[72];
		int edgeCount = 0;
		int edgeId = 0;

		// Go through each tile to find edges
		for (Tile tile : tiles) {
			if (tile == null) continue;

			int[] nodeIds = tile.getNodeIds();

			// For each tile, create 6 edges (connecting consecutive nodes)
			for (int i = 0; i < 6; i++) {
				int nodeIdA = nodeIds[i];
				int nodeIdB = nodeIds[(i + 1) % 6];  // Wrap around: last connects to first

				// Check if this edge already exists
				boolean edgeExists = false;
				for (int j = 0; j < edgeCount; j++) {
					Edge existingEdge = edges[j];
					int edgeNodeA = existingEdge.getNodeA().getId();
					int edgeNodeB = existingEdge.getNodeB().getId();

					// Check both orders: (A,B) or (B,A)
					if ((edgeNodeA == nodeIdA && edgeNodeB == nodeIdB) ||
						(edgeNodeA == nodeIdB && edgeNodeB == nodeIdA)) {
						edgeExists = true;
						break;
					}
				}

				if (!edgeExists) {

					// Create new edge
					Node nodeA = nodes[nodeIdA];
					Node nodeB = nodes[nodeIdB];
					Edge newEdge = new Edge(edgeId++, nodeA, nodeB);
					edges[edgeCount++] = newEdge;
				}
			}
		}


		board.setEdges(edges);
	}
}

