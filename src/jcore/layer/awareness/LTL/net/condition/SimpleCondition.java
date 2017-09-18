package layer.awareness.LTL.net.condition;

import java.util.ArrayList;

import layer.semantic.Condition;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.fol.syntax.Negation;
import net.sf.tweety.logics.fol.syntax.RelationalFormula;
import net.sf.tweety.lp.asp.syntax.DLPAtom;
import net.sf.tweety.lp.asp.syntax.DLPLiteral;
import net.sf.tweety.lp.asp.syntax.DLPNeg;

/**
 * This Class is used for a Simple Condition, that is a First Order Logic Proposition to check if it's true or false in the system domain.
 */
public class SimpleCondition extends TransitionCondition {

	/** The condition that must be entailed in the system domain */
	private Condition cond;
	
	/** The default predicate */
	private DLPLiteral defaultPred;
	
	/** The negated predicate */
	private DLPLiteral negPred;
	
	/** The state condition that indicates if the formula must be accepted or error */
	private String stateCondition;
	
	/** The negation condition of predicate */
	private boolean neg;
	
	/** The predicate */
	private String pred;
	
	/** The args. */
	private ArrayList<String> args;
	
	/**
	 * Instantiates a new simple condition.
	 *
	 * @param term
	 *            the term
	 * @param pred
	 *            the pred
	 * @param args
	 *            the args
	 * @param neg
	 *            the neg
	 */
	public SimpleCondition(String term, String pred, ArrayList<String> args, boolean neg) {
		super(term);
		stateCondition = "A";
		this.pred = pred;
		this.args = args;
		this.neg = neg;
		
//		FOLAtom atom = new FOLAtom( new Predicate(pred, args.size()) );
//		HashSet<Variable> argSet = new HashSet<>();
//		for( String s : args ){
//			Variable tmp = new Variable(s);
//			atom.addArgument(tmp);
//			argSet.add(tmp);
//		} 
//		
//		//cond = new Condition(atom); 
//		cond = new Condition( new ExistsQuantifiedFormula(atom, argSet) );
		
		ArrayList<Constant> cs = new ArrayList<>();
		for( String s : args )
			cs.add(new Constant(s));
		
		if(neg){
			defaultPred = new DLPNeg(new DLPAtom(pred, cs));
			negPred = new DLPAtom(pred, cs);
		}
		else{
			defaultPred = new DLPAtom(pred, cs);
			negPred = new DLPNeg(new DLPAtom(pred, cs));
		}
		
		cond = new Condition(defaultPred);
	}
	
	/**
	 * Instantiates a copy of a simple condition.
	 *
	 * @param copy
	 *            the copy
	 */
	public SimpleCondition( SimpleCondition copy ) {
		this(copy.getTerm(), copy.pred, copy.args, copy.neg);
	}

	/* (non-Javadoc)
	 * @see layer.awareness.LTL.net.condition.TransitionCondition#setStateCondition(java.lang.String)
	 */
	@Override
	public void setStateCondition(String s) {
		if( s.equals("E") ){
			//cond = new Condition( new Negation((ExistsQuantifiedFormula) cond.getFOLFormula()) );
			//cond = new Condition(new Negation( (RelationalFormula) cond.getFOLFormula()));
			cond = new Condition(negPred);
			stateCondition = s;
			if( !getTerm().startsWith("!") )
				setTerm("!" + getTerm());
		}
		else if( s.equals("A") ){
			//cond = new Condition( new Negation((ExistsQuantifiedFormula) cond.getFOLFormula()) );
			//cond = new Condition(new Negation( (RelationalFormula) cond.getFOLFormula()));
			cond = new Condition(defaultPred);
			stateCondition = s;
			if( getTerm().startsWith("!") )
				setTerm(getTerm().substring(1));
		}
	}
	
	/**
	 * Gets the condition.
	 *
	 * @return the condition
	 */
	public Condition getCondition() {
		return cond;
	}
	
	/**
	 * Gets the state condition.
	 *
	 * @return the state condition
	 */
	public String getStateCondition() {
		return stateCondition;
	}

}
