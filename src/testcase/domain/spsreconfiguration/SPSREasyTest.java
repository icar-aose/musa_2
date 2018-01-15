package domain.spsreconfiguration;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.icar.musa.core.Requirements;
import org.icar.musa.core.domain.StateOfWorld;
import org.icar.musa.core.runtime_entity.AbstractCapability;
import org.icar.musa.core.runtime_entity.AssumptionSet;
import org.icar.musa.core.runtime_entity.ProblemSpecification;
import org.icar.musa.exception.ProblemDefinitionException;
import org.icar.musa.proactive_means_end_reasoning.ExtendedNode;
import org.icar.musa.proactive_means_end_reasoning.GraphExpansion;
import org.icar.musa.proactive_means_end_reasoning.ProblemExploration;
import org.icar.musa.proactive_means_end_reasoning.SolutionGraph;
import org.icar.musa.proactive_means_end_reasoning.wts.WorldNode;
import org.icar.specification.linear_temporal_logic.formulamodel.LTLGoal;
import org.icar.specification.linear_temporal_logic.net.PNHierarchy;
import org.icar.specification.linear_temporal_logic.net.TokenConf;
import org.junit.Before;
import org.junit.Test;

import jason.stdlib.perceive;

public class SPSREasyTest {

	private AssumptionSet assumptions;
	private Requirements requirements;
	private ArrayList<AbstractCapability> allCap;
	private ArrayList<AbstractCapability> capSet1;
	private ArrayList<AbstractCapability> capSet2;
	private ArrayList<AbstractCapability> capSet3;
	private ProblemSpecification ps;
	private StateOfWorld first;
	
	
	@Before
	public void init() {
		SPSReconfigurationEasy s = new SPSReconfigurationEasy();
		assumptions = s.getDomainAssumptions();
		requirements = s.getRequirements();
		allCap = s.getCapabilitySet();
		capSet1 = s.getSubCapabilitySet1();
		capSet2 = s.getSubCapabilitySet2();
		capSet3 = s.getSubCapabilitySet3();
		first = s.getInitialState();
		
		ps = new ProblemSpecification(assumptions, requirements, null);
		
	}
	
	@Test
	public void test() {
		SolutionGraph graph = new SolutionGraph();
		ProblemExploration pe = null;
		try {
			pe = new ProblemExploration(ps, allCap);
		} catch (ProblemDefinitionException e) {
			System.out.println("I goal devono essere specificati in LTL");
		}
		pe.addToVisit(new WorldNode(first), new TokenConf(new PNHierarchy((LTLGoal) requirements)), 10);
		
		int k = 0;
		// simula il ciclo di espansione di un WTS
		while( !pe.terminated() && k++ < 100) {
			//System.out.println("ciclo "+k);
			pe.expandNode();
			pe.log_current_state();
			
			
			GraphExpansion exp = pe.getHighestExpansion();
			pe.removeExpandedNode(exp);
			
			//System.out.println("expand "+exp.getCapability());
			
			//update graph
			graph.addNode(exp);
			
			//update problem exploration with new nodes
			for(ExtendedNode node : exp.getDestination()){
				//System.out.println("new "+node.getWorldState());
				pe.addToVisit(new WorldNode(node.getWorldState()), node.getTokens(), node.getScore() );
			}
		}
		
		graph.printForGraphviz();
	}

}
