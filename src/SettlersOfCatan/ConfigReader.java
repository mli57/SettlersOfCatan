package SettlersOfCatan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigReader {

    private static final String configFile = "game.config";
    private static final int minTurns = 1;
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


    public static int readTurns(String filename) throws IOException {
        try (BufferedReader reader = nwe BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null){
                line = line.trim();


                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }


                if (line.startsWith("turns:")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2){
                        try {
                            int turns = Integer.parseInt(parts[1].trim());

                            if (turns < minTurns || turns > maxTurns){
                                throw new IllegalArgumentException("Turns msut be between " + minTurns + " and " + maxTurns + ". Found: " + turns); 
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