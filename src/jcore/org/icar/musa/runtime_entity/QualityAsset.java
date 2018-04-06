package org.icar.musa.runtime_entity;

import org.icar.musa.context.StateOfWorld;

public abstract class QualityAsset implements RunTimeEntity {
	public abstract long max_score();
	public abstract long evaluate_state(StateOfWorld w);
	public abstract double getThreshold_metrics();
	
	/* for debug */
	public abstract String getShortStateRepresentation(StateOfWorld w);
	public abstract void log_state(AssumptionSet assumptions, StateOfWorld w);
}
