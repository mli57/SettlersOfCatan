package SettlersOfCatan;

/**
 * Abstract base for structures placed on nodes (Settlement, City).
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public abstract class Building {
	/** The player who owns this building **/
	private final Player owner;

	/**
	 * Constructor for a building.
	 * @param owner The player who owns this building
	 */
	public Building(Player owner) {
		// Set building owner
		this.owner = owner;
	}

	/**
	 * Gets the owner of this building.
	 * @return The player who owns this building
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Gets the victory points this building contributes.
	 * @return The number of victory points
	 */
	public abstract int getVictoryPoints();

	/**
	 * Checks if this building can be upgraded to a city.
	 * @return true if upgradeable, false otherwise
	 */
	public abstract boolean canUpgradeToCity();

	/**
	 * Gets the resource multiplier for this building (1 for Settlement, 2 for City).
	 * @return The multiplier for resource production
	 */
	public abstract int getResourceMultiplier();
}
