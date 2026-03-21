package SettlersOfCatan;

/**
 * Command that encapsulates a city build request (settlement upgrade) as a self-contained object (R3.1).
 * Stores the settlement that was previously on the node so it can be fully restored on undo.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class BuildCityCommand implements GameCommand {

    /** The node where the city will be placed */
    private final Node node;

    /** The player building the city */
    private final Player player;

    /** The bank for resource transactions */
    private final Bank bank;

    /** The PlayerActions instance for placement logic */
    private final PlayerActions actions;

    /** The settlement that existed on the node before the upgrade, stored on first execute */
    private Building previousBuilding;

    /**
     * Constructs a BuildCityCommand.
     * @param node    The node where the city will be placed
     * @param player  The player building the city
     * @param bank    The bank for resource transactions
     * @param actions The PlayerActions instance for placement logic
     */
    public BuildCityCommand(Node node, Player player, Bank bank, PlayerActions actions) {
        this.node = node;
        this.player = player;
        this.bank = bank;
        this.actions = actions;
    }

    /**
     * Executes the city placement, capturing the previous building before upgrading.
     */
    @Override
    public void execute() {
        previousBuilding = node.getBuilding();
        actions.placeCity(node, player);
    }

    /**
     * Undoes the city placement, restoring the previous settlement and refunding resources.
     */
    @Override
    public void undo() {
        node.setBuilding(previousBuilding);
        node.setOccupyingPlayer(player);
        bank.refundCity(player);
        actions.refreshVisualizerState();
    }
}
