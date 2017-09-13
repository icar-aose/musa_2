package layer.awareness.LTL.net.netmodels;

import layer.awareness.LTL.net.*;
import petrinet.logic.Transition;

public class RelasePN extends FormulaPN {

	public RelasePN( TransitionCondition op1, TransitionCondition op2 ) {
		super();
		this.firstOp = op1;
		TransitionCondition firstOpCopy;
		TransitionCondition firstOpCopyCopy;
		if( firstOp instanceof SimpleCondition ){
			firstOpCopy = new SimpleCondition(firstOp.getTerm());
			firstOpCopyCopy = new SimpleCondition(firstOp.getTerm());
		}
		else{
			firstOpCopy = new FormulaCondition(firstOp.getTerm());
			firstOpCopyCopy = new FormulaCondition(firstOp.getTerm());
		}
		this.secondOp = op2;
		TransitionCondition secondOpCopy;
		if( secondOp instanceof SimpleCondition )
			secondOpCopy = new SimpleCondition(secondOp.getTerm());
		else
			secondOpCopy = new FormulaCondition(secondOp.getTerm());
		
		start = pn.place("Start");
		association.put(start, "A");
		
		Transition t1 = pn.transition(firstOp.getTerm());
		firstOp.setStateCondition("A");
		labels.put(t1, firstOp);
		
		pn.arc("a1", start, t1);
		pn.arc("a2", t1, start);
		
		Transition t2 = pn.transition(firstOp.getTerm() + "-" + secondOp.getTerm());
		firstOpCopy.setStateCondition("A");
		secondOpCopy.setStateCondition("A");
		CombinationCondition firstCombination = new CombinationCondition(firstOpCopy, secondOpCopy); 
		labels.put(t1, firstCombination);
		
		pn.arc("a3", start, t2);
		association.put(pn.arc("a2", t2, pn.place("Accept")).getPlace(), "A");

		Transition t3 = pn.transition("RERR-" + firstOp.getTerm() + "-" + secondOp.getTerm());
		firstOpCopyCopy.setStateCondition("E");
		secondOpCopy.setStateCondition("E");
		CombinationCondition secondCombination = new CombinationCondition(firstOpCopyCopy, secondOpCopy); 
		labels.put(t1, secondCombination);
		
		pn.arc("a4", start, t3);
		association.put(pn.arc("a2", t3, pn.place("Error")).getPlace(), "E");
	}
}
