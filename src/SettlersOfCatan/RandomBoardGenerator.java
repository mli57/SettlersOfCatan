package SettlersOfCatan;

import java.util.Random;

/**
 * Generates a random Catan board with terrain and token distribution.
 * SOLID: Single Responsibility - only handles board generation.
 * GRASP: Pure Fabrication - creates board structure.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class RandomBoardGenerator implements IBoardGenerator {
	/** Random number generator for board generation **/
	private final Random random;

	/**
	 * Constructor with default random seed.
	 */
	public RandomBoardGenerator() {
		this.random = new Random();
	}

	/**
	 * Constructor with specified random seed for reproducible boards.
	 * @param seed The random seed
	 */
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
			{1, 2, 3, 4, 5, 0},    		// Tile  0
			{6, 7, 8, 9, 3, 2}, 	// Tile  1
			{3, 9, 10, 11, 12, 4}, 	// Tile  2
			{5, 4, 12, 14, 15, 13},  	// Tile  3
			{17, 0, 5, 13, 18, 16}, 		// Tile  4
			{20, 21, 1, 0, 17, 19},  	// Tile  5
			{22, 23, 6, 2, 1, 21}, 	// Tile  6
			{24, 25, 26, 27, 8, 7},	// Tile  7
			{8, 27, 28, 29, 10, 9},  	// Tile  8
			{10, 29, 30, 31, 32, 11}, 	// Tile  9
			{12, 11, 32, 33, 34, 14}, 	// Tile 10
			{15, 14, 34, 36, 37, 35}, 	// Tile 11
			{18, 13, 15, 35, 39, 38}, 	// Tile 12
			{41, 16, 18, 38, 42, 40}, 	// Tile 13
			{44, 19, 17, 16, 41, 43}, 	// Tile 14
			{46, 47, 20, 19, 44, 45}, 	// Tile 15
			{48, 49, 22, 21, 20, 47}, 	// Tile 16
			{50, 51, 52, 23, 22, 49}, 	// Tile 17
			{52, 53, 24, 7, 6, 23}    // Tile 18
		};

		int[][] boardCoords = {
			{0, 0, 0}, {0, -1, 1}, {-1, 0, 1},
			{-1, 1, 0}, {0, 1, -1}, {1, 0, -1},
			{1, -1, 0}, {0, -2, 2}, {-1, -1, 2},
			{-2, 0, 2}, {-2, 1, 1}, {-2, 2, 0},
			{-1, 2, -1}, {0, 2, -2}, {1, 1, -2},
			{2, 0, -2}, {2, -1, -1}, {2, -2, 0},
			{1, -2, 1}
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
					
					// Set up node adjacencies
					nodeA.addAdjacentNode(nodeB);
					nodeB.addAdjacentNode(nodeA);
				}
			}
		}

		// Set up tile adjacencies for nodes
		for (Tile tile : tiles) {
			if (tile == null) continue;
			int[] nodeIds = tile.getNodeIds();
			for (int nodeId : nodeIds) {
				nodes[nodeId].addAdjacentTile(tile);
			}
		}

		board.setEdges(edges);
	}
}

