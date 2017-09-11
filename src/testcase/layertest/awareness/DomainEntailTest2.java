package layertest.awareness;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import layer.awareness.DomainEntail;
import layer.semantic.AssumptionSet;
import layer.semantic.Condition;
import layer.semantic.StateOfWorld;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.lp.asp.parser.ParseException;
import net.sf.tweety.lp.asp.syntax.DLPAtom;

/**
 * DomainEntail TestCase 2
 * @author Mirko Zichichi
 */
public class DomainEntailTest2 {

	/** A State of the World w */
	StateOfWorld w;

	/** The domain. */
	AssumptionSet domain;

	@Before
	public void init() {
		this.w = new StateOfWorld();
		try {
			w.addFact_asString("penguin(tweety).");
			w.addFact_asString("parrot(polly).");
			w.addFact_asString("sparrow(sid).");
			w.addFact_asString("broken_wing(sid).");
			w.addFact_asString("ostrich(olga).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}

		this.domain = new AssumptionSet();
		try {
			domain.addAssumption_asString("bird(X) :- penguin(X).");
			domain.addAssumption_asString("bird(X) :- parrot(X).");
			domain.addAssumption_asString("bird(X) :- sparrow(X).");
			domain.addAssumption_asString("bird(X) :- ostrich(X).");
			domain.addAssumption_asString("cannot_fly(X) :- penguin(X).");
			domain.addAssumption_asString("cannot_fly(X) :- ostrich(X).");
			domain.addAssumption_asString("cannot_fly(X) :- broken_wing(X).");
			domain.addAssumption_asString("can_fly(X) :- bird(X), not cannot_fly(X).");

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAnAssumptionSet e) {
			e.printStackTrace();
		}
	}

	/**
	 * Entails condition test 1.
	 * Testing if polly ( a parrot -> which is a bird -> and has not broken wings ) can fly.
	 */
	@Test
	public void entailsConditionTest1(){
		DomainEntail env = DomainEntail.getInstance();

		Constant polly = new Constant("polly");
		DLPAtom q1 = new DLPAtom("can_fly", polly);
		Condition cond_q1 = new Condition(q1);

		assertTrue( env.entailsCondition(w, domain, cond_q1) );
	}

	/**
	 * Entails condition test 2.
	 * Testing if sid ( a sparrow -> which is a bird -> and has broken wings ) can fly.
	 */
	@Test
	public void entailsConditionTest2(){
		DomainEntail env = DomainEntail.getInstance();

		Constant sid = new Constant("sid");
		DLPAtom q2 = new DLPAtom("can_fly", sid);
		Condition cond_q2 = new Condition(q2);

		assertFalse( env.entailsCondition(w, domain, cond_q2) );

	}

	/**
	 * Entails condition test 3.
	 * Testing if sid ( a sparrow -> which is a bird -> and has broken wings ) can fly.
	 */
	@Test
	public void entailsConditionTest3(){
		DomainEntail env = DomainEntail.getInstance();

		Constant tweety = new Constant("tweety");
		DLPAtom q3 = new DLPAtom("can_fly", tweety);
		Condition cond_q3 = new Condition(q3);

		assertTrue( env.entailsCondition(w, domain, cond_q3) );

	}

}
