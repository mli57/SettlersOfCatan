package SettlersOfCatan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for reading game configuration from config file.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class ConfigReader {
	/** Path to the configuration file **/
    private static final String configFile = "src/SettlersOfCatan/game.config";
    
    /** Minimum number of turns allowed **/
    private static final int minTurns = 1;
    
    /** Maximum number of turns allowed **/
    private static final int maxTurns = 8192;


    /**
     * Reads the number of turns from the config file.
     * 
     * @return the number of turns specified in the config file
     * @throws IOException if the config file cannot be read
     * @throws IllegalArgumentException if the turns value is invalid
     */
    public static int readTurns() throws IOException {
        return readTurns(configFile); 
    }


    /**
     * Reads the number of turns from a specified config file.
     * @param filename The path to the config file
     * @return The number of turns specified in the config file
     * @throws IOException if the config file cannot be read
     * @throws IllegalArgumentException if the turns value is invalid
     */
    public static int readTurns(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line; // Current line being read

            // Read each line of the config file
            while ((line = reader.readLine()) != null){
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Check for turns configuration
                if (line.startsWith("turns:")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2){
                        try {
                            // Parse the number of turns
                            int turns = Integer.parseInt(parts[1].trim());

                            // Validate turn count is within allowed range
                            if (turns < minTurns || turns > maxTurns){
                                throw new IllegalArgumentException("Turns must be between " + minTurns + " and " + maxTurns + ". Found: " + turns); 
                            }

                            return turns;
                        } catch (NumberFormatException e){
                            throw new IllegalArgumentException("Invalid number format for turns: " + parts[1].trim());
                        }
                    }
                }
            }

            throw new IllegalArgumentException("Config file must contain 'turns: <value>' where value is between " + minTurns + " and " + maxTurns);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private ConfigReader(){
        throw new AssertionError("ConfigReader should not be instantiated, it is a utility class only");
    }
}