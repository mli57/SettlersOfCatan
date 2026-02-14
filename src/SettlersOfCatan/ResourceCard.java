package SettlersOfCatan;

/**
 * A resource card held by a player (type is one of the five resources).
 * @author Kabir Singh Sachdeva, Adrian Najmi, Sarthak Kulashari, Maxwell Li
 */
public class ResourceCard {
	/** The type of resource this card represents **/
	private final ResourceType type;

	/**
	 * Constructor for a resource card.
	 * @param type The type of resource
	 */
	public ResourceCard(ResourceType type) {
		this.type = type;
	}

	/**
	 * Gets the resource type of this card.
	 * @return The resource type
	 */
	public ResourceType getType() {
		return type;
	}
}
