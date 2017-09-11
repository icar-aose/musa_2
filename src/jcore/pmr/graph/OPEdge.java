package pmr.graph;

/**
 * The Class OPEdge. it Connects a WorldNode to an OPNode. Is stored inside the
 * OPNode
 */
public class OPEdge implements Edge {

	/** The source. */
	private WorldNode source;

	/** The destination. */
	private OPNode destination;

	/** The capability. */
	private String capability;

	private String agent;

	/**
	 * Instantiates a new OP edge.
	 *
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @param capability
	 *            the capability
	 */
	public OPEdge(WorldNode source, OPNode destination, String capability) {
		super();
		this.source = source;
		this.destination = destination;
		this.capability = capability;
	}

	public OPEdge(WorldNode source, OPNode destination, String capability, String agent) {
		super();
		this.source = source;
		this.destination = destination;
		this.capability = capability;
		this.agent = agent;
	}

	/**
	 * Gets the capability.
	 *
	 * @return the capability, stored in the edge
	 */
	public String getCapability() {
		return this.capability;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source of this edge.
	 */
	public WorldNode getSource() {
		return this.source;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getAgent() {
		return this.agent;
	}

	/**
	 * Gets the destination.
	 *
	 * @return the destination of this edge.
	 */
	public OPNode getDestination() {
		return this.destination;
	}

	/**
	 * Sets the source.
	 *
	 * @param source
	 *            the new source
	 */
	public void setSource(WorldNode source) {
		this.source = source;
	}

	/**
	 * Sets the destination.
	 *
	 * @param destination
	 *            the new destination
	 */
	public void setDestination(OPNode destination) {
		this.destination = destination;
	}

	/**
	 * Sets the scenario.
	 *
	 * @param capability
	 *            the new scenario
	 */
	public void setScenario(String capability) {
		this.capability = capability;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NormalEdge) {
			NormalEdge temp = (NormalEdge) obj;

			if (temp.getSource() == null && this.source != null)
				return false;
			else if (temp.getSource() != null && this.source == null)
				return false;

			if (temp.getDestination() == null && this.destination != null)
				return false;
			else if (temp.getDestination() != null && this.destination == null)
				return false;

			if (this.source == null && temp.getSource() == null &&
				this.destination == null && temp.getDestination() == null)
					return true;

			if (!this.source.equals(temp.getSource()))
				return false;

			if (temp.getDestination().getOPNodeList().contains(this.destination))
				return true;

			return false;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.source.hashCode() + this.destination.hashCode() + this.capability.hashCode();
	}
}
