package SettlersOfCatan;

import java.util.Random;

/**
 * Rolls dice for the game.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class DiceRoller implements Dice {
	/** Random number generator for dice rolls **/
	private final Random random = new Random();

	/**
	 * Rolls two dice and returns their sum.
	 * @param sides The number of sides on each die
	 * @return The sum of two dice rolls
	 */
	@Override
	public int rollTwoDice(int sides) {
		// Roll two dice and sum the results
		return roll(sides) + roll(sides);
	}

	/**
	 * Rolls a single die.
	 * @param sides The number of sides on the die
	 * @return A random number from 1 to sides
	 */
	@Override
	public int roll(int sides) {
		// Generate random number from 1 to sides
		return random.nextInt(sides) + 1;
	}
}
