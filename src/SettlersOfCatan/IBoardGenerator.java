package SettlersOfCatan;

/**
 * Interface for board generation strategies.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public interface IBoardGenerator {
	/**
	 * Generates and populates a board with tiles, nodes, and edges.
	 * @param board the board to populate
	 */
	void generate(Board board);
}

