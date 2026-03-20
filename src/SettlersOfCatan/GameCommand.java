package SettlersOfCatan;

/**
 * Command interface for the Command design pattern (Assignment 3 Part 1)
 * Instead of calling placement methods directly, the human turn creates a command object, 
 * passes it to CommandHistory which executes and stores it, and
 * undo/redo simply operate on those stored objects.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public interface GameCommand {

    /**
     * Executes the command, applying the build action to the game state.
     */
    void execute();

    /**
     * Undoes the command, reversing the build action from the game state.
     */
    void undo();
}
