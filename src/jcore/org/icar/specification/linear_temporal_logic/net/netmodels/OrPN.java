package datalayer.awareness.LTL.net.netmodels;

import datalayer.awareness.LTL.net.PetriNetState;
import datalayer.awareness.LTL.net.condition.CombinationCondition;
import datalayer.awareness.LTL.net.condition.FormulaCondition;
import datalayer.awareness.LTL.net.condition.SimpleCondition;
import datalayer.awareness.LTL.net.condition.TransitionCondition;
import petrinet.logic.Transition;

/**
 * The Class OrPN, used to create a PetriNet that models an OR formula.
 */
public class OrPN extends FormulaPN {

	/**
	 * Instantiates a new or PN.
	 *
	 * @param op1
	 *            the op 1
	 * @param op2
	 *            the op 2
	 */
	public OrPN( TransitionCondition op1, TransitionCondition op2 ) {
		super("OrPN");
		this.firstOp = op1;
		TransitionCondition firstOpCopy;
		if( firstOp instanceof SimpleCondition )
			firstOpCopy = new SimpleCondition((SimpleCondition) firstOp);
		else
			firstOpCopy = new FormulaCondition(firstOp.getTerm());
		this.secondOp = op2;
		TransitionCondition secondOpCopy;
		if( secondOp instanceof SimpleCondition )
			secondOpCopy = new SimpleCondition((SimpleCondition) secondOp);
		else
			secondOpCopy = new FormulaCondition(secondOp.getTerm()); 
		
		start = pn.place("Start");
		placeState.put(start, PetriNetState.WAIT_BUT);
		
		Transition t1 = pn.transition(firstOp.getTerm());
		firstOp.setStateCondition(PetriNetState.ACCEPTED);
		transitionLabel.put(t1, firstOp);
		
		pn.arc("a1", start, t1);
		placeState.put(pn.arc("a2", t1, pn.place("Accept1")).getPlace(), PetriNetState.ACCEPTED);

		Transition t2 = pn.transition("ORERR-" + firstOp.getTerm() + "-" + secondOp.getTerm());
		firstOpCopy.setStateCondition(PetriNetState.ERROR);
		secondOpCopy.setStateCondition(PetriNetState.ERROR);
		transitionLabel.put(t2, new CombinationCondition(firstOpCopy, secondOpCopy));
				
		pn.arc("a3", start, t2);
		placeState.put(pn.arc("a4", t2, pn.place("Error")).getPlace(), PetriNetState.ERROR);
		
		Transition t3 = pn.transition(secondOp.getTerm());
		secondOp.setStateCondition(PetriNetState.ACCEPTED);
		transitionLabel.put(t3, secondOp);
		
		pn.arc("a5", start, t3);
		placeState.put(pn.arc("a6", t3, pn.place("Accept2")).getPlace(), PetriNetState.ACCEPTED);
		
	}
}
