package SettlersOfCatan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Final chain handler that scores and executes the best available action.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class ValueScoringHandler extends ActionHandler {
	/**
	 * Creates a new handler.
	 * @param random shared random number generator
	 */
	public ValueScoringHandler(Random random) {
		super(random);
	}

	@Override
	public void handleTurn(Player player, PlayerActions actions, int roundCount) {
		List<String> available = actions.getAvailableActions(player, false);
		if (available.isEmpty()) {
			System.out.println(player.getColor() + " - No available actions");
			return;
		}

		double bestScore = Double.NEGATIVE_INFINITY;
		List<String> best = new ArrayList<>();
		for (String action : available) {
			double score = scoreAction(player, action);
			if (score > bestScore) {
				bestScore = score;
				best.clear();
				best.add(action);
			} else if (score == bestScore) {
				best.add(action);
			}
		}

		String chosen = best.get(random.nextInt(best.size()));
		execute(chosen, player, actions, roundCount);
	}

	private double scoreAction(Player player, String action) {
		if ("SETTLEMENT".equals(action)) {
			return 1.0;
		}
		if ("CITY".equals(action)) {
			return 1.0;
		}
		if ("ROAD".equals(action)) {
			int cardsAfterRoad = countRealCards(player) - 2;
			return cardsAfterRoad < 5 ? 0.5 : 0.8;
		}
		return 0.0; // PASS
	}

	private int countRealCards(Player player) {
		int total = 0;
		for (Map.Entry<ResourceType, Integer> entry : player.getResources().entrySet()) {
			if (entry.getKey() == ResourceType.NULL) {
				continue;
			}
			total += entry.getValue();
		}
		return total;
	}

	private void execute(String action, Player player, PlayerActions actions, int roundCount) {
		if ("SETTLEMENT".equals(action)) {
			actions.buildSettlement(player, roundCount);
			return;
		}
		if ("CITY".equals(action)) {
			actions.buildCity(player, roundCount);
			return;
		}
		if ("ROAD".equals(action)) {
			actions.buildRoad(player, roundCount);
			return;
		}
		System.out.println(roundCount + " / " + player.getColor() + ": Pass");
	}
}

