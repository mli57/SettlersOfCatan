package SettlersOfCatan;

/**
 * Settlement: 1 victory point; can be upgraded to City.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Settlement extends Building {
	/** Victory points awarded for a settlement **/
	private static final int VICTORY_POINTS = 1;

	/**
	 * Constructor for a settlement.
	 * @param owner The player who owns this settlement
	 */
	public Settlement(Player owner) {
		// Initialize settlement
		super(owner);
	}

	/**
	 * Gets the victory points for a settlement.
	 * @return 1 victory point
	 */
	@Override
	public int getVictoryPoints() {
		return VICTORY_POINTS;
	}

	/**
	 * Checks if settlement can be upgraded to a city.
	 * @return true (settlements can be upgraded)
	 */
	@Override
	public boolean canUpgradeToCity() {
		return true;
	}

	/**
	 * Gets the resource multiplier for a settlement.
	 * @return 1 (settlements produce 1 resource per roll)
	 */
	@Override
	public int getResourceMultiplier() {
		return 1;
	}
}
