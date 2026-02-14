package SettlersOfCatan;

/**
 * City: 2 victory points; produces double resources when adjacent tiles roll.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class City extends Building {
	/** Victory points awarded for a city **/
	private static final int VICTORY_POINTS = 2;

	/**
	 * Constructor for a city.
	 * @param owner The player who owns this city
	 */
	public City(Player owner) {
		// Initialize city
		super(owner);
	}

	/**
	 * Gets the victory points for a city.
	 * @return 2 victory points
	 */
	@Override
	public int getVictoryPoints() {
		return VICTORY_POINTS;
	}

	/**
	 * Checks if city can be upgraded (cities cannot be upgraded further).
	 * @return false (cities are already at maximum level)
	 */
	@Override
	public boolean canUpgradeToCity() {
		return false;
	}

	/**
	 * Gets the resource multiplier for a city.
	 * @return 2 (cities produce 2 resources per roll)
	 */
	@Override
	public int getResourceMultiplier() {
		return 2;
	}
}
