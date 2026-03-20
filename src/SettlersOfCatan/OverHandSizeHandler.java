package SettlersOfCatan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Forces spending when a player holds more than 7 real cards.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class OverHandSizeHandler extends ActionHandler {
	/**
	 * Creates a new handler.
	 * @param random shared random number generator
	 */
	public OverHandSizeHandler(Random random) {
		super(random);
	}

	@Override
	public void handleTurn(Player player, PlayerActions actions, int roundCount) {
		int realCards = 0;
		for (Map.Entry<ResourceType, Integer> entry : player.getResources().entrySet()) {
			if (entry.getKey() == ResourceType.NULL) {
				continue;
			}
			realCards += entry.getValue();
		}

		if (realCards > 7) {
			List<String> available = actions.getAvailableActions(player, true);
			String chosen = chooseBestNonPass(available);
			if (chosen != null) {
				execute(chosen, player, actions, roundCount);
				return;
			}
		}

		if (successor != null) {
			successor.handleTurn(player, actions, roundCount);
		}
	}

	private String chooseBestNonPass(List<String> available) {
		double bestScore = Double.NEGATIVE_INFINITY;
		List<String> best = new ArrayList<>();
		for (String action : available) {
			if ("PASS".equals(action)) {
				continue;
			}
			double score = score(action);
			if (score > bestScore) {
				bestScore = score;
				best.clear();
				best.add(action);
			} else if (score == bestScore) {
				best.add(action);
			}
		}
		if (best.isEmpty()) {
			return null;
		}
		return best.get(random.nextInt(best.size()));
	}

	private double score(String action) {
		if ("SETTLEMENT".equals(action) || "CITY".equals(action)) {
			return 1.0;
		}
		if ("ROAD".equals(action)) {
			return 0.8;
		}
		return 0.0;
	}

	private void execute(String action, Player player, PlayerActions actions, int roundCount) {
		if ("SETTLEMENT".equals(action)) {
			actions.buildSettlement(player, roundCount);
		} else if ("CITY".equals(action)) {
			actions.buildCity(player, roundCount);
		} else if ("ROAD".equals(action)) {
			actions.buildRoad(player, roundCount);
		}
	}
}

