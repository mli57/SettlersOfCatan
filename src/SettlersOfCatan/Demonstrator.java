package SettlersOfCatan;

import java.io.IOException;

/**
 * Demonstrator class that runs a sample Catan simulation.
 * This demonstrates the key functionality of the simulator.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class Demonstrator {
    
    /**
     * Main method to run the Catan game simulation.
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            // Step 1: Read configuration
            // Reads the number of rounds from game.config file
            int maxRounds = ConfigReader.readTurns();
            System.out.println("=== SETTLERS OF CATAN SIMULATOR ===");
            System.out.println("Configuration: Maximum " + maxRounds + " rounds\n");
            
            // Step 2: Set up the game board
            // Create board and initialize with tiles, nodes, edges
            Board board = new Board();
            IBoardGenerator generator = new RandomBoardGenerator();
            generator.generate(board);
            
            // Step 3: Create game components using dependency injection
            Dice dice = new DiceRoller();          // Handles dice rolling
            Bank bank = new Bank();                // Handles resource payments
            IPlacementValidator validator = new PlacementValidator();  // Validates placements
            
            // Step 4: Create game with 4 players
            Game game = new Game(board, dice, bank, validator, 4);
            
            // Step 5: Run the simulation
            // This includes:
            // - Setup phase (each player places 2 settlements + 2 roads)
            // - Main game loop (roll dice, distribute resources, build)
            // - Game ends when someone reaches 10 VP or max rounds reached
            System.out.println("Starting game simulation...\n");
            game.startGame(maxRounds);
            
        } catch (IOException e) {
            // Handle file reading errors
            System.err.println("ERROR: Could not read game.config file");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Make sure game.config exists with format: turns: <number>");
        } catch (IllegalArgumentException e) {
            // Handle invalid configuration values
            System.err.println("ERROR: Invalid configuration");
            System.err.println("Details: " + e.getMessage());
        }
    }
}

