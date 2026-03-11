import org.junit.*;


import SettlersOfCatan.Node;

/**
 * Unit tests for the {@link Node} class.
 * Focuses on the distance rule in {@code canPlaceBuilding}.
 */
public class NodeTest {

	/**
	 * Boundary test: isolated node with no building and no adjacent nodes
	 * should allow placing a building.
	 */
	@Test
	public void canPlaceBuildingReturnsTrueForIsolatedEmptyNode() {
		Node node = new Node(0);

		assertTrue(node.canPlaceBuilding());
	}

	/**
	 * Boundary test: node with an adjacent node that already has a building
	 * should not allow placing a new building here.
	 */
	@Test
	public void canPlaceBuildingReturnsFalseWhenAdjacentNodeHasBuilding() {
		Node center = new Node(1);
		Node neighbor = new Node(2);

		center.addAdjacentNode(neighbor);
		neighbor.addAdjacentNode(center);

		neighbor.setBuilding(new SettlersOfCatan.Building(SettlersOfCatan.BuildingType.SETTLEMENT));

		assertFalse(center.canPlaceBuilding());
	}

	/**
	 * Boundary test: node already occupied should not allow another building,
	 * even if all adjacent nodes are empty.
	 */
	@Test
	public void canPlaceBuildingReturnsFalseWhenNodeAlreadyHasBuilding() {
		Node node = new Node(3);

		node.setBuilding(new SettlersOfCatan.Building(SettlersOfCatan.BuildingType.SETTLEMENT));

		assertFalse(node.canPlaceBuilding());
	}
}
