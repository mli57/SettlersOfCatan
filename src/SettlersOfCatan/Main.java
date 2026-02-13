package SettlersOfCatan;

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        
        // Print board information at the start
        System.out.println("=== CATAN GAME START ===");
        game.getBoard().printTiles();
        game.getBoard().printNodes();
        
        game.intializePlayers();
        game.setupInitialSettlements();
    }
}