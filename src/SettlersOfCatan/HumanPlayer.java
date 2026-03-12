package SettlersOfCatan;

import java.util.Map;

/**
 * A human-controlled player. Extends Player with no changes to game logic —
 * the only difference is isHuman() returns true so Game knows to read from
 * the console instead of picking actions randomly.
 *
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class HumanPlayer extends Player {

    public HumanPlayer(PlayerColor color) {
        super(color);
    }

    /** Tells the game loop this player needs console input. */
    public boolean isHuman() {
        return true;
    }

    /**
     * Returns a human-readable summary of the player's current resource hand.
     * Used by the List command. Skips resource types with zero count and NULL.
     *
     * @return formatted hand string, e.g. "WOOD: 2, BRICK: 1" or "No cards."
     */
    public String formatHand() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> entry : getResources().entrySet()) {
            if (entry.getKey() == ResourceType.NULL || entry.getValue() == 0){
                continue;
            }
            if (sb.length() > 0){
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        
        if (sb.length() == 0) {
            return "No cards.";
        } 
        else {
            return sb.toString();
        }
    }
}
