package SettlersOfCatan;

/**
 * Command that encapsulates a road build request as a self-contained object (R3.1).
 * Stores all data needed to execute and undo the action: the target edge, the player,
 * the bank for refunds, and the PlayerActions instance for placement logic.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class BuildRoadCommand implements GameCommand {

    /** The edge where the road will be placed **/
    private final Edge edge;

    /** The player building the road **/
    private final Player player;

    /** The bank for resource transactions **/
    private final Bank bank;

    /** The PlayerActions instance for placement logic **/
    private final PlayerActions actions;

    /**
     * Constructs a BuildRoadCommand.
     * @param edge    The edge where the road will be placed
     * @param player  The player building the road
     * @param bank    The bank for resource transactions
     * @param actions The PlayerActions instance for placement logic
     */
    public BuildRoadCommand(Edge edge, Player player, Bank bank, PlayerActions actions) {
        this.edge = edge;
        this.player = player;
        this.bank = bank;
        this.actions = actions;
    }

    /**
     * Executes the road placement via PlayerActions.
     */
    @Override
    public void execute() {
        actions.placeRoad(edge, player);
    }

    /**
     * Undoes the road placement, removing the road and refunding resources.
     */
    @Override
    public void undo() {
        edge.setRoad(null);
        bank.refundRoad(player);
        actions.refreshVisualizerState();
    }
}
