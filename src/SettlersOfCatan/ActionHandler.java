package SettlersOfCatan;

import java.util.Random;

/**
 * Base class for rule-based AI handlers using Chain of Responsibility.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public abstract class ActionHandler {
	/** Next handler in the chain. */
	protected ActionHandler successor;

	/** Shared random source for tie-breaking decisions. */
	protected final Random random;

	/**
	 * Creates a handler using the shared random instance.
	 * @param random shared random number generator
	 */
	public ActionHandler(Random random) {
		this.random = random;
	}

	/**
	 * Sets the next handler in the chain.
	 * @param successor next handler
	 */
	public void setSuccessor(ActionHandler successor) {
		this.successor = successor;
	}

	/**
	 * Handles one AI turn decision.
	 * @param player current AI player
	 * @param actions game action facade
	 * @param roundCount current round count for logging
	 */
	public abstract void handleTurn(Player player, PlayerActions actions, int roundCount);
}

