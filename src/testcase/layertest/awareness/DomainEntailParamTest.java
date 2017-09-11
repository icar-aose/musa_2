package layertest.awareness;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import layer.awareness.DomainEntail;
import layer.semantic.AssumptionSet;
import layer.semantic.Condition;
import layer.semantic.StateOfWorld;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.lp.asp.parser.ParseException;
import net.sf.tweety.lp.asp.syntax.DLPAtom;

@RunWith(Parameterized.class)
public class DomainEntailParamTest {

	DomainEntail env;
	StateOfWorld w;
	AssumptionSet domain;
	Condition cond;

	@Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                 { "tweety", "can_fly"}, { "polly", "can_fly" }, {"sid", "can_fly"}, {"olga", "cannot_fly"}
           });
    }

	public DomainEntailParamTest( String one, String two ) {
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

		env = DomainEntail.getInstance();

		Constant cons = new Constant(one);
		DLPAtom q = new DLPAtom(two, cons);
		cond = new Condition(q);
	}

	@Test
	public void test(){
		assertTrue( env.entailsCondition(w, domain, cond));
	}
}
