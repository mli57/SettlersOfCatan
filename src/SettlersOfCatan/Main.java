package SettlersOfCatan;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Ask for number of players
        int numPlayers = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.println("How many players are playing? (2-4)");
            try {
                numPlayers = scanner.nextInt();
                if (numPlayers >= 2 && numPlayers <= 4) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a number between 2 and 4.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
        
        // Create game with number of players and scanner
        Game game = new Game(numPlayers, scanner);
        
        // Print board information at the start
        System.out.println("\n=== CATAN GAME START ===");
        game.getBoard().printTiles();
        game.getBoard().printNodes();
        
        // Start the game
        game.startGame();
    }
}