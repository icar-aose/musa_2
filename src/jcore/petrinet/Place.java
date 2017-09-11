package petrinet;

import java.util.ArrayList;
import java.util.List;

public class Place extends PetrinetObject {

	// it's a magic number...
	public static final int UNLIMITED = -1;

	private int tokens = 0;
	private int maxTokens = UNLIMITED;

	/** Added */
	private List<Arc> incoming = new ArrayList<Arc>();
	/** Added */
	private List<Arc> outgoing = new ArrayList<Arc>();

	protected Place(String name) {
		super(name);
	}

	protected Place(String name, int initial) {
		this(name);
		this.tokens = initial;
	}

	/**
	 * besitzt die Stelle mindestens so viele tokens?
	 *
	 * @param threshold
	 * @return
	 */
	public boolean hasAtLeastTokens(int threshold) {
		return (tokens >= threshold);
	}

	/**
	 * w rde die Stelle noch so viele Tokens aufnehmen k nnen?
	 *
	 * @param newTokens
	 * @return
	 */
	public boolean maxTokensReached(int newTokens) {
		if (hasUnlimitedMaxTokens()) {
			return false;
		}

		return (tokens + newTokens > maxTokens);
	}

	private boolean hasUnlimitedMaxTokens() {
		return maxTokens == UNLIMITED;
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;
	}

	public void setMaxTokens(int max) {
		this.maxTokens = max;
	}

	public void addTokens(int weight) {
		this.tokens += weight;
	}

	public void removeTokens(int weight) {
		this.tokens -= weight;
	}

	/**
	 * Added Aggiunge un Arco in entrata
	 *
	 * @param arc
	 */
	public void addIncoming(Arc arc) {
		this.incoming.add(arc);
	}

	/** Added */
	public void setIncoming(List<Arc> incoming) {
		this.incoming = incoming;
	}

	/** Added */
	public List<Arc> getIncoming() {
		return incoming;
	}

	/**
	 * Added Aggiunge un Arco in uscita
	 *
	 * @param arc
	 */
	public void addOutgoing(Arc arc) {
		this.outgoing.add(arc);
	}

	/** Added */
	public void setOutgoing(List<Arc> outgoing) {
		this.outgoing = outgoing;
	}

	/** Added */
	public List<Arc> getOutgoing() {
		return outgoing;
	}

	/** Added */
	public boolean equals(Place place) {
		if (this.getName().equals(place.getName()))
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return super.toString() + " Tokens=" + this.tokens + " max="
				+ (hasUnlimitedMaxTokens() ? "unlimited" : this.maxTokens);
	}
}
