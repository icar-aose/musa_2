package org.icar.ltlpetrinet.hierarchical_model;

import org.icar.ltlpetrinet.annotated_pn.AnnotatedPlace;
import org.icar.ltlpetrinet.annotated_pn.PNStateEnum;

import petrinet.logic.Place;

import org.icar.musa.core.context.StateOfWorld;
import org.icar.musa.core.runtime_entity.AssumptionSet;

public abstract class LogicNode extends HierarchyNode {
	private HierarchyNode left;
	private HierarchyNode right;
	
	private int predicate_sum_scores;
	private int tot_predicate_slots;

	public LogicNode(String name,HierarchyNode left, HierarchyNode right) {
		super(name);
		this.left = left;
		this.right = right;
		add_dependency("left", left);
		add_dependency("right", right);
		
		tot_predicate_slots=0;
		predicate_sum_scores=0;
	}
	
	public void init() {
		left.init();
		right.init();
	}

	public int getPredicate_sum_scores() {
		return predicate_sum_scores;
	}

	public void setPredicate_sum_scores(int predicate_sum_scores) {
		this.predicate_sum_scores = predicate_sum_scores;
	}

	public int getTot_predicate_slots() {
		return tot_predicate_slots;
	}

	public void setTot_predicate_slots(int tot_predicate_slots) {
		this.tot_predicate_slots = tot_predicate_slots;
	}

	public HierarchyNode getLeft() {
		return left;
	}

	public HierarchyNode getRight() {
		return right;
	}

	@Override
	public void updateNet(StateOfWorld w,AssumptionSet assumptions) {
		left.updateNet(w,assumptions);
		right.updateNet(w,assumptions);
	}
	
	protected boolean retrieveState_forFatherTransitionDependency(StateOfWorld w, AssumptionSet assumptions, boolean normal) {
		boolean normal_test = false;
		
		boolean left_dep = left.retrieveState_forFatherTransitionDependency(w,assumptions,normal);
		boolean right_dep = right.retrieveState_forFatherTransitionDependency(w,assumptions,normal);
		normal_test = fireable_truth_table(left_dep,right_dep);

		return normal_test;
	}

//	private void update_predicate_slots_and_scores() {
//		if (tot_predicate_slots==0)
//			tot_predicate_slots = count_slots();
//		
//		predicate_sum_scores = sum_predicate_true();
//	}
//
//	private int sum_predicate_true() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

//	public abstract int count_slots();
//
	protected abstract boolean fireable_truth_table(boolean op1, boolean op2);

	@Override
	public PNStateEnum getNetState() {
		PNStateEnum left_state = left.getNetState();
		PNStateEnum right_state = right.getNetState();
		
		return state_truth_table(left_state,right_state);
	}

	protected abstract PNStateEnum state_truth_table(PNStateEnum op1, PNStateEnum op2);

}
