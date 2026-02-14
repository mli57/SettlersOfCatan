package SettlersOfCatan;

/**
 * Interface for dice rolling functionality.
 */
public interface Dice {
	/**
	 * Rolls two dice and returns the sum.
	 * @param sides number of sides per die
	 * @return sum of two dice rolls
	 */
	int rollTwoDice(int sides);

	/**
	 * Rolls a single die.
	 * @param sides number of sides
	 * @return result of the roll (1 to sides)
	 */
	int roll(int sides);
}

