package SettlersOfCatan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builds roads defensively when opponents are close to matching longest road.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class DefendRoadHandler extends ActionHandler {
	private final Player[] players;
	private final Board board;

	/**
	 * Creates a new handler.
	 * @param players all players in the game
	 * @param board game board
	 * @param random shared random number generator
	 */
	public DefendRoadHandler(Player[] players, Board board, Random random) {
		super(random);
		this.players = players;
		this.board = board;
	}

	@Override
	public void handleTurn(Player player, PlayerActions actions, int roundCount) {
		int myLongest = getLongestRoad(player);
		boolean threatened = false;
		for (Player other : players) {
			if (other == null || other == player) {
				continue;
			}
			int otherLongest = getLongestRoad(other);
			if (otherLongest >= myLongest - 1) {
				threatened = true;
				break;
			}
		}

		if (threatened && player.canBuildRoad()) {
			actions.buildRoad(player, roundCount);
			return;
		}

		if (successor != null) {
			successor.handleTurn(player, actions, roundCount);
		}
	}

	private int getLongestRoad(Player player) {
		Edge[] allEdges = board.getEdges();
		if (allEdges == null) {
			return 0;
		}

		List<Edge> owned = new ArrayList<>();
		for (Edge edge : allEdges) {
			if (edge != null && edge.getRoad() != null && edge.getRoad().getOwner() == player) {
				owned.add(edge);
			}
		}
		int best = 0;
		for (Edge start : owned) {
			List<Edge> visited = new ArrayList<>();
			int len = dfsRoadLength(start, owned, visited);
			if (len > best) {
				best = len;
			}
		}
		return best;
	}

	private int dfsRoadLength(Edge current, List<Edge> owned, List<Edge> visited) {
		visited.add(current);
		int bestContinuation = 0;
		for (Edge next : owned) {
			if (next == current || visited.contains(next)) {
				continue;
			}
			if (!current.isAdjacentTo(next)) {
				continue;
			}
			List<Edge> branchVisited = new ArrayList<>(visited);
			int continuation = dfsRoadLength(next, owned, branchVisited);
			if (continuation > bestContinuation) {
				bestContinuation = continuation;
			}
		}
		return 1 + bestContinuation;
	}
}

