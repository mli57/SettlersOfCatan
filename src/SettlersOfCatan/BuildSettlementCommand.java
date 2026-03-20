package SettlersOfCatan;

/**
 * Command that encapsulates a settlement build request as a self-contained object (R3.1).
 * Stores all data needed to execute and undo the action: the target node, the player,
 * the bank for refunds, and the PlayerActions instance for placement logic.
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class BuildSettlementCommand implements GameCommand {

    /** The node where the settlement will be placed **/
    private final Node node;

    /** The player building the settlement **/
    private final Player player;

    /** The bank for resource transactions **/
    private final Bank bank;

    /** The PlayerActions instance for placement logic **/
    private final PlayerActions actions;

    /**
     * Constructs a BuildSettlementCommand.
     * @param node    The node where the settlement will be placed
     * @param player  The player building the settlement
     * @param bank    The bank for resource transactions
     * @param actions The PlayerActions instance for placement logic
     */
    public BuildSettlementCommand(Node node, Player player, Bank bank, PlayerActions actions) {
        this.node = node;
        this.player = player;
        this.bank = bank;
        this.actions = actions;
    }

    /**
     * Executes the settlement placement via PlayerActions.
     */
    @Override
    public void execute() {
        actions.placeSettlement(node, player);
    }

    /**
     * Undoes the settlement placement, removing the building and refunding resources.
     */
    @Override
    public void undo() {
        node.setBuilding(null);
        node.setOccupyingPlayer(null);
        bank.refundSettlement(player);
        actions.refreshVisualizerState();
    }
}
