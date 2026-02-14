package SettlersOfCatan;

public class Main {
    public static void main(String[] args) {
        // Fixed to 4 players
        int numPlayers = 4;
        
        // Create dependencies
        Board board = new Board();
        Dice dice = new DiceRoller();
        Bank bank = new Bank();
        IPlacementValidator validator = new PlacementValidator();
        IBoardGenerator generator = new RandomBoardGenerator();
        
        // Generate board
        generator.generate(board);
        
        // Create game with dependency injection
        Game game = new Game(board, dice, bank, validator, numPlayers);
        
        // Print board information at the start
        System.out.println("\n=== CATAN GAME START ===");
        BoardPrinter.printTiles(board);
        BoardPrinter.printNodes(board);
        
        // Start the game
        game.startGame();
    }
}