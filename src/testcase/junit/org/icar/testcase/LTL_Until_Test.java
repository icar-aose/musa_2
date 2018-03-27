package org.icar.testcase;

import static org.junit.Assert.*;

import org.icar.ltlpetrinet.annotated_pn.PNStateEnum;
import org.icar.ltlpetrinet.hierarchical_model.NetHierarchy;
import org.icar.ltlpetrinet.hierarchical_model.PredicateNode;
import org.icar.ltlpetrinet.hierarchical_model.template.UntilPN;
import org.icar.ltlpetrinet.supervisor.NetSupervisor;
import org.icar.ltlpetrinet.supervisor.TokenConf;
import org.icar.musa.core.context.StateOfWorld;
import org.icar.musa.core.fol_reasoner.FOLCondition;
import org.icar.musa.core.runtime_entity.AssumptionSet;
import org.icar.musa.utils.exception.NotAllowedInAStateOfWorld;
import org.junit.Before;
import org.junit.Test;

import net.sf.tweety.lp.asp.parser.ParseException;

public class LTL_Until_Test {
	private NetSupervisor supervisor;
	
	@Before
	public void init() {

		PredicateNode pn1 = new PredicateNode("pn1", new FOLCondition("lit(a)"));
		PredicateNode pn2 = new PredicateNode("pn2", new FOLCondition("lit(b)"));
		UntilPN root = new UntilPN("un1", pn1, pn2);		
		root.init();
		
		NetHierarchy h = new NetHierarchy(root);
		TokenConf tokens = h.getInitialTokenConfiguration();
		
		supervisor = new NetSupervisor(h, tokens);
	}
	@Test
	public void test() {
		StateOfWorld w1 = new StateOfWorld();
		StateOfWorld w2 = new StateOfWorld();
		StateOfWorld w3 = new StateOfWorld();
		try {
			w1.addFact_asString("lit(a).");
			w2.addFact_asString("lit(a).");w2.addFact_asString("lit(c).");
			w3.addFact_asString("lit(c).");w3.addFact_asString("lit(b).");
		} catch (ParseException e) {
			e.printStackTrace();
		}  catch (NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		
		AssumptionSet assumptions = new AssumptionSet();
		
		supervisor.prepareTokens();
		supervisor.update(w1,assumptions);
		assertEquals(supervisor.getState(),PNStateEnum.WAIT_BUT_ERROR);

		supervisor.update(w2,assumptions);
		assertEquals(supervisor.getState(),PNStateEnum.WAIT_BUT_ERROR);

		supervisor.update(w3,assumptions);
		assertEquals(supervisor.getState(),PNStateEnum.ACCEPTED);

		supervisor.cleanTokens();
	}

}
