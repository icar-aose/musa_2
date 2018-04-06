package org.icar.testcase.pmr;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.icar.linear_temporal_logic.hierarchical_ltl_model.NetHierarchy;
import org.icar.linear_temporal_logic.hierarchical_ltl_model.NetHierarchyBuilder;
import org.icar.musa.applications.monitoring_workflow.WakeUp;
import org.icar.musa.context.StateOfWorld;
import org.icar.musa.pmr.problem_exploration.ProblemExploration;
import org.icar.musa.pmr.problem_exploration.ProblemSpecification;
import org.icar.musa.pmr.problem_exploration.StateNode;
import org.icar.musa.pmr.problem_exploration.WTS;
import org.icar.musa.pmr.problem_exploration.WTSExpansion;
import org.icar.musa.runtime_entity.AbstractCapability;
import org.icar.musa.runtime_entity.AssumptionSet;
import org.icar.musa.runtime_entity.Requirements;
import org.icar.musa.utils.exception.ProblemDefinitionException;
import org.icar.specification.LTLgoal.specmodel.LTLGoal;
import org.junit.Before;
import org.junit.Test;

public class CapabilityEvolution {
	private AssumptionSet assumptionsWU;
	private Requirements requirementsWU;
	private ArrayList<AbstractCapability> allCapWU;
	private ArrayList<AbstractCapability> capSet1WU;
	private ArrayList<AbstractCapability> capSet2WU;
	private ArrayList<AbstractCapability> capSet3WU;
	private ProblemSpecification ps_WU;
	private StateOfWorld initial_state_WU;

	@Before
	public void initWakeUp() {
		WakeUp s = new WakeUp();
		assumptionsWU = s.getDomainAssumptions();
		requirementsWU = s.getRequirements();
		allCapWU = s.getCapabilitySet();
		initial_state_WU = s.getInitialState();
		
		ps_WU = new ProblemSpecification(assumptionsWU, requirementsWU, null);	
	}

	@Test
	public void test() throws ProblemDefinitionException {
		ProblemExploration pe = null;
		StateNode root = new StateNode(initial_state_WU);
		
		LTLGoal mygoal = (LTLGoal) ps_WU.getGoal_specification();
		NetHierarchyBuilder builder = new NetHierarchyBuilder();
		NetHierarchy nets = builder.build(mygoal);
		WTS wts = new WTS(root);

		pe = new ProblemExploration(ps_WU, allCapWU,"myagent");	
		
		//AbstractCapability cap = null;
		for (AbstractCapability cap : allCapWU) {
			if (cap.getId().equals("check_posture")) {
				//WTSExpansion exp = pe.test_generate_cap_evolution(root,cap);
				//exp.printForGraphviz();
			}
		}
		
		
	}

}
