package SettlersOfCatan;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages the undo/redo history of GameCommand objects (R3.1).
 * Uses two stacks: history for executed commands and redoStack for undone commands.
 * Executing a new command clears the redo stack, preserving linear history.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class CommandHistory {

    /** Stack of executed commands available for undo */
    private Deque<GameCommand> history;

    /** Stack of undone commands available for redo */
    private Deque<GameCommand> redoStack;

    /**
     * Constructs a new CommandHistory with empty history and redo stacks.
     */
    public CommandHistory() {
        this.history = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
    }

    /**
     * Executes a command, pushes it onto the history stack, and clears the redo stack.
     * Clearing the redo stack ensures that branching histories do not occur.
     * @param action The command to execute and record
     */
    public void pushToStack(GameCommand action) {
        action.execute();
        history.push(action);
        redoStack.clear();
    }

    /**
     * Undoes the most recent command, moving it to the redo stack.
     * Prints "Nothing to undo." if no commands are in history.
     */
    public void undo() {
        if (!canUndo()) {
            System.out.println("Nothing to undo.");
            return;
        }
        GameCommand action = history.pop();
        action.undo();
        redoStack.push(action);
    }

    /**
     * Redoes the most recently undone command, moving it back to the history stack.
     * Prints "Nothing to redo." if no commands are in the redo stack.
     */
    public void redo() {
        if (!canRedo()) {
            System.out.println("Nothing to redo.");
            return;
        }
        GameCommand action = redoStack.pop();
        action.execute();
        history.push(action);
    }

    /**
     * Returns whether there are commands available to undo.
     * @return true if there is at least one command in history
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }

    /**
     * Returns whether there are commands available to redo.
     * @return true if there is at least one command in the redo stack
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
