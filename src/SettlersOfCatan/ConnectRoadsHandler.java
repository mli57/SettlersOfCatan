package SettlersOfCatan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Prioritizes roads when it can connect nearby disconnected road segments.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class ConnectRoadsHandler extends ActionHandler {
	private final Board board;

	/**
	 * Creates a new handler.
	 * @param board game board
	 * @param random shared random number generator
	 */
	public ConnectRoadsHandler(Board board, Random random) {
		super(random);
		this.board = board;
	}

	@Override
	public void handleTurn(Player player, PlayerActions actions, int roundCount) {
		if (player.canBuildRoad() && hasCloseDisconnectedRoads(player)) {
			actions.buildRoad(player, roundCount);
			return;
		}

		if (successor != null) {
			successor.handleTurn(player, actions, roundCount);
		}
	}

	private boolean hasCloseDisconnectedRoads(Player player) {
		Edge[] allEdges = board.getEdges();
		if (allEdges == null) {
			return false;
		}

		List<Edge> owned = new ArrayList<>();
		for (Edge edge : allEdges) {
			if (edge != null && edge.getRoad() != null && edge.getRoad().getOwner() == player) {
				owned.add(edge);
			}
		}
		if (owned.size() < 2) {
			return false;
		}

		for (int i = 0; i < owned.size(); i++) {
			for (int j = i + 1; j < owned.size(); j++) {
				Edge a = owned.get(i);
				Edge b = owned.get(j);
				if (a.isAdjacentTo(b)) {
					continue;
				}
				if (distanceWithinTwoHops(a, b, allEdges)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean distanceWithinTwoHops(Edge start, Edge target, Edge[] graphEdges) {
		Queue<Edge> queue = new ArrayDeque<>();
		Queue<Integer> depth = new ArrayDeque<>();
		List<Edge> visited = new ArrayList<>();
		queue.add(start);
		depth.add(0);
		visited.add(start);

		while (!queue.isEmpty()) {
			Edge current = queue.poll();
			int d = depth.poll();
			if (current == target) {
				return d <= 2;
			}
			if (d >= 2) {
				continue;
			}
			for (Edge candidate : graphEdges) {
				if (candidate == null) {
					continue;
				}
				if (!current.isAdjacentTo(candidate)) {
					continue;
				}
				if (visited.contains(candidate)) {
					continue;
				}
				visited.add(candidate);
				queue.add(candidate);
				depth.add(d + 1);
			}
		}
		return false;
	}
}

