package reasoner.probexp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import datalayer.awareness.AbstractCapability;
import datalayer.awareness.AssumptionSet;
import datalayer.awareness.ProblemSpecification;
import datalayer.awareness.LTL.formulamodel.LTLGoal;
import datalayer.awareness.LTL.net.*;
import datalayer.awareness.LTL.net.condition.*;
import datalayer.world.StateOfWorld;
import datalayer.world.WorldEvolution;
import datalayer.world.evolution.EvolutionScenario;
import datalayer.world.wts.WorldNode;
import exception.ProblemDefinitionException;
import petrinet.logic.Arc;
import petrinet.logic.Transition;
import reasoner.EntailOperator;
 
/**
 * The Artifact ProblemExploration. It's used by an Agent to perform evolution for the various StateOfWorld passed, using his Capability.
 * It keeps a list of all the expanded nodes ready for bidding in auctions.
 * @author Alessandro Fontana
 * @author Mirko Zichichi
 */
// nota di Luca: in questa versione del ProblemExploration, i goal ammessi sono solo quelli LTL
public class ProblemExploration {
	private String my_agent_name = "";
	
	/** The Assumption Set defined globally that the Agent has to maintain */
	private AssumptionSet assumptions;
	
	/** The Capabilities List that an Agent holds */
	private ArrayList<AbstractCapability> capabilities;
	
	/** The sorted List of ENode to visit */
	private ArrayList<ExtendedNode> toVisit;
	
	/** The List of visited ENode */
	private Set<StateOfWorld> visited;
	
	///** The List of visited ENode */
	//private Set<ExtendedNode> all_states;

	/** The sorted List that contains all the ExpansionNodes generated by the Agent  */
	private ArrayList<GraphExpansion> expandedList;
	
	/** Nets generated from the LTL Formula*/
	private PNHierarchy nets;
	
	private int iterations=0;
	
	private boolean verbose=false;
	
//	boolean terminated=false;
	
	/**
	 * Instantiates a new ProblemExploration.
	 *
	 * @param model
	 *            the Formula Model to follow
	 * @param capabilities
	 *            the Capabilities List proper of the Agent
	 * @param assumptions
	 *            the assumptions
	 * @throws ProblemDefinitionException 
	 */
	public ProblemExploration( ProblemSpecification ps, ArrayList<AbstractCapability> capabilities, String agent_name) throws ProblemDefinitionException {
		initialize(ps,capabilities);
		setAgentName(agent_name);
	}

	
	public ProblemExploration( ProblemSpecification ps, ArrayList<AbstractCapability> capabilities) throws ProblemDefinitionException {
		initialize(ps,capabilities);
	}
	
	private void initialize(ProblemSpecification ps, ArrayList<AbstractCapability> capabilities) throws ProblemDefinitionException {
		this.capabilities = new ArrayList<>(capabilities);
		this.assumptions = ps.getAssumptions();
		toVisit = new ArrayList<>();
		visited = new HashSet<>();
		expandedList = new ArrayList<>();
		ends = new HashMap<String, Boolean>();
		//all_states = new HashSet<ExtendedNode>();
		
		if (!(ps.getGoal_specification() instanceof LTLGoal)) {
			throw new ProblemDefinitionException();
		} else {
			LTLGoal model = (LTLGoal) ps.getGoal_specification();
			nets = new PNHierarchy(model);
		}
	}
	
	
	public void setAgentName(String name) {
		my_agent_name = name;
	}
	
//	public boolean isTerminated() {
//		return terminated;
//	}
	

	/**
	 * This Operation is used to add a new world node to the toVisit List.
	 *
	 * @param node
	 *            the WorldNode from SolutionGraph
	 * @param tokens
	 *            the Tokens List associated to that node 
	 * @param score
	 *            the Score associated to that node
	 */
	public void addToVisit( WorldNode node, TokensConfiguration tokens, int score) {
		//all_states.add(new ExtendedNode(node.getWorldState(), tokens, score, false, false));
		
		if( visited.contains(node.getWorldState())) {
			//System.out.println("Nodo "+node.getWorldState().toSortedString()+" già visitato");
			return;
		}
		if (toVisit.contains(new ExtendedNode(node.getWorldState()))) {
			//System.out.println("Nodo "+node.getWorldState().toSortedString()+" già presente tra i nodi da vistare");
			return;
		}
		//System.out.println("Nuovo Nodo da visitare"+node.getWorldState().toSortedString());
		toVisit.add( new ExtendedNode(node.getWorldState(), tokens, score, false, false) );		
	}
	
	/**
	 * The main operation, it's used to expand an ENode from the toVisit List.
	 * It entails if one of is Capability is compatible with the StateOfWorld contained in the ENode.
	 * If so, it calls a set of methods used to ultimate the expansion, in order: applyExpand, applyNets, score.
	 * Finally, whatever the case is, it adds the ENode to the visited List. 
	 */
	public void expandNode() {
		iterations++;
		ExtendedNode enode = getHighestNodeToVisit();

		if(enode == null) {
//			if (terminated==false)
//				log_current_state();
//			terminated=true;
			return;
		}
		
		if(verbose) System.out.println("Visito node :"+enode.getWorldState().toString());
		visited.add(enode.getWorldState());
		
		for( int i = 0; i < capabilities.size(); i++ ){
			AbstractCapability capability = capabilities.get(i);
			if(EntailOperator.getInstance().entailsCondition(enode.getWorldState(), this.assumptions, capability.getPreCondition()) == true){
				//Starts the expansion
				if(verbose) System.out.println("\n-----------------------------------------------------------------");
				if(verbose) System.out.println("Applying capability " + capability.getId() );
				GraphExpansion expNode = applyExpand(enode, capability);

				
				boolean toAdd=true;
				if (expNode != null){
					//Applies the net to ultimate the expansion						
					for( ExtendedNode destination : expNode.getDestination() ){
						applyNets(expNode.getSource().getTokens(), destination);
						if(verbose) System.out.print("generated arc to node :"+destination.getWorldState().toString() );
						if(!destination.isErrorNode())	{
							if (destination.isExitNode()) {
								if(verbose) System.out.println("EXIT" );
							} else {
								if(verbose) System.out.println("VALID" );
							}
							
							//this.addToVisit(new WorldNode(destination.getWorldState()), destination.getTokens(), destination.getScore());
						} else {
							toAdd=false;
							if(verbose) System.out.println("NOT VALID" );
						}
					}
					
					if (toAdd==true) {
						//Elaborates the Expansion score				
						score(expNode);
										
						//Adds the Expansion to the List in order 
						expandedList.add(expNode);
					}
				}
			}
		}
		if(verbose) log_current_state();
	}	
	
	/**
	 * Operation used to get the highest ExpansionNode
	 *
	 * @return the highest ExpansionNode
	 */
	public GraphExpansion getHighestExpansion(){
		
		if(this.expandedList.size() == 0)	return null;
		
		expandedList.sort(GraphExpansion.getScoreComparator());
//		System.out.println("--------------expanded---------------");
//		for (GraphExpansion ex : expandedList) {
//			for (ExtendedNode n : ex.getDestination() ) {
//				System.out.println(ex.getSource().getWorldState().toSortedString()+"->"+ex.getCapability()+"->"+n.getWorldState().toSortedString()+"=>"+ex.hashCode()+" [score("+ex.getScore()+")]");
//			}
//		}

		
		int index = this.expandedList.size() - 1;
		GraphExpansion selected = this.expandedList.get(index);
		
//		for (ExtendedNode n : selected.getDestination() ) {
//			System.out.println("Selected: "+selected.getSource().getWorldState().toSortedString()+"->"+selected.getCapability()+"->"+n.getWorldState().toSortedString()+"=>"+selected.hashCode()+" [score("+selected.getScore()+")]");
//		}
		
		return selected;
	}
	
	public void removeExpandedNode(GraphExpansion node){
		if(node == null)	return;
		//System.out.println("size before: "+expandedList.size());
		for (ExtendedNode n : node.getDestination() ) {
			//System.out.println("rimuovo "+node.getSource().getWorldState().toSortedString()+"->"+node.getCapability()+"->"+n.getWorldState().toSortedString()+"=>"+node.hashCode());
		}
				
		this.expandedList.remove(node);
		
		//log_current_state();
		
		//System.out.println("size after: "+expandedList.size());
//		if(this.expandedList.remove(node))
//			System.out.println("HO RIMOSSO IL NODO.");
	}
	
	/**
	 * Method that creates the expanded States of World from the ENode, applying the evolution scenario from the capability
	 *
	 * @param enode
	 *            the ENode
	 * @param capability
	 *            the capability
	 * @return the expansion node
	 */
	private GraphExpansion applyExpand( ExtendedNode enode, AbstractCapability capability) {
		//Simple evolution, managed by NormalExpansionNode
		if(capability.getScenarioSet().size() == 1){
			//WorldEvolution is given by the AssumptionSet and the StateOfWorld
			WorldEvolution evo = new WorldEvolution(this.assumptions, enode.getWorldState());
			
			//Without iterator I can't access to the elements in ScenarioSet. It's just one element, the last one in the list
			//of evolutions. Anyway WorldEvolution saves the StateOfWorld source.
			Iterator<EvolutionScenario> i = capability.getScenarioSet().iterator();
			if(i.hasNext()){
				EvolutionScenario temp =(EvolutionScenario) i.next();
				evo.addEvolution(temp.getOperators());
			}
		
			ArrayList<ExtendedNode> newEnodeList = new ArrayList<ExtendedNode>();
			if(evo.getEvolution().getLast().equals(enode.getWorldState()) == false){
				ExtendedNode newEnode = new ExtendedNode(evo.getEvolution().getLast());
				newEnodeList.add(newEnode);
				//String scenario = (String)capability.getScenarioSet().iterator().next().getName();
				GraphExpansion result = new NormalExpansion(enode, newEnodeList, capability.getId(),my_agent_name);
				return result;
			}
			else	return null;
		}
		else{
			//When Capability's got more than one Scenario, I have to create a WorldEvolution for each Scenario. Every WorldEvolution 
			//will produce a StateOfWorld, that would be stored in a Node (that would get to the destinations list in MultipleExpansioNode. 
			//It will also update the Node-Scenario map with the Node-Scenario entry.
			MultipleExpansion expNode = new MultipleExpansion(enode, new ArrayList<ExtendedNode>(), capability.getId(),my_agent_name);
			
			Iterator<EvolutionScenario> i = capability.getScenarioSet().iterator();
			while(i.hasNext()){
				WorldEvolution evo = new WorldEvolution(this.assumptions, enode.getWorldState());
				EvolutionScenario temp = (EvolutionScenario) i.next();
				evo.addEvolution(temp.getOperators());
				if(evo.getEvolution().getLast().equals(enode.getWorldState()) == false){
					ExtendedNode newEnode = new ExtendedNode(evo.getEvolution().getLast());
					expNode.addDestination(newEnode);
					expNode.addScenario(newEnode, temp.getName());
				}
			}
			GraphExpansion result = expNode;
			return result;
		}
	}
	
	/**
	 * After the expansion a new ENode has been created, but it's empty. This
	 * method fills up the remaining attributes using Nets.
	 * 
	 * It starts applying the tokens from the tokens configuration to the Nets.
	 * Then it will scan the First Net (the one contained into the tree model root)
	 * and recursively will check the state of some nets to generate a new token
	 * configuration. Finally it fills up the ENode with the new configuration and 
	 * calculates a new score for the ENode.
	 * The SCORE is elaborated using a function that follows a color rule.
	 * The function sets a promising node at a value near 0 to represent BLACK, 
	 * 255 for WHITE, in between to represent GRAY. 
	 *
	 * @param startingTokens
	 *            the configuration of tokens to start with
	 * @param enode
	 *            the new eNode created from expansion
	 */	
	private void applyNets( TokensConfiguration startingTokens, ExtendedNode enode ) {
		//System.out.println("\n|||||||||||||||||||||||\n Expanded World State:\n" + enode.getWorldState());
		StateOfWorld state = enode.getWorldState();
		TokensConfiguration tokens = new TokensConfiguration(startingTokens);
		
		HashSet<String> visitedNets = new HashSet<>();
		
		//Prepares the net with tokens
		nets.putTokens(tokens);
		
		//Checking compatibility with StateOfWorld through every net, starting from first
		superviseNet(tokens, nets.getStartingNet(), state, visitedNets);
		
		//Fills up ENode
		enode.setTokens(tokens);
		
		//Checking if it's an exit node
		enode.checkNodeType( nets.getStartingPN().getNetState() );
		
		//Calculating Hops
		double nHop = nets.hop(); 
		
		//Elaborating score
		int score = (int)(255 * nHop);
		enode.setScore(score);
		
		//Cleans the net from tokens
		nets.removeTokens();
	}
	
	/**
	 * This method is used inside applyNets to scan a Net checking if its State is
	 * Accepted, Error or Waiting. For every transition able to fire in the net, 
	 * it checks if the new state of world entails the condition labeled in the 
	 * transition. The condition can be divided in three different type: Formula, 
	 * Atomic Proposition and Combined Formula. If it's a Formula, the associated 
	 * Net would be scanned recursively using formulaCheck. If it's an Atomic 
	 * Proposition, it would be entailed by DomainEntails. If it's a Combined Formula,
	 * it holds other two conditions (either Formula or Atomic Proposition) to be 
	 * satisfied with a logical conjunction. When a condition is satisfied, the 
	 * associated transition will fire.
	 *
	 * @param tokens
	 *            the tokens
	 * @param net
	 *            the net
	 * @param state
	 *            the state
	 * @param visitedNets
	 *            the visited nets
	 */
	private void superviseNet( TokensConfiguration tokens, String net, StateOfWorld state, HashSet<String> visitedNets ) {
		visitedNets.add(net);
		//Checking compatibility with StateOfWorld for every Transition for Firing
		for( Transition t : (ArrayList<Transition>) nets.getTransitionsAbleToFire(net) ){
			if( t.canFire() ){
				//Take a different path if the transition has a condition containing a Formula, a Atomic Proposition  or a Composed Formula 
				TransitionCondition tCond = nets.getTransitionCondition(net, t);
				//Formula
				if( tCond instanceof FormulaCondition ){
					//System.out.println("-Net:("+net+")\nStarting checking "+ tCond.getTerm() + " [F] ");
					if( formulaCheck((FormulaCondition)tCond, tokens, state, visitedNets) )
						fire(t, tokens, net);	//Fires if the condition matches with the state
					//System.out.println("Finished checking "+ tCond.getTerm() + " [F] in Net:("+net+")");
				}
				//Atomic Proposition 
				else if( tCond instanceof SimpleCondition ){
					//System.out.println("-Net:("+net+")\nStarting checking "+ tCond.getTerm() + " [S] ");
					if( EntailOperator.getInstance().entailsCondition(state, assumptions, ((SimpleCondition) tCond).getCondition()) )
						fire(t, tokens, net);
					//System.out.println("Finished checking "+ tCond.getTerm() + " [S] in Net:("+net+")");
				}
				//Composition
				else if( tCond instanceof CombinationCondition ){
					//System.out.println("-Net:("+net+")\nStarting checking "+ tCond.getTerm() + " [C] ");
					double[] tmpArr = {0.0, 0.0};
					int count = 0;
					for( TransitionCondition tCCond : ((CombinationCondition) tCond).getCond() ) {
						//Formula
						if( tCCond instanceof FormulaCondition ){
							//System.out.println("Starting checking "+ tCCond.getTerm() + " [CF] ");
							if( formulaCheck((FormulaCondition)tCCond, tokens, state, visitedNets) ){
								tmpArr[count++] = 1.0;//Fires if the condition matches with the state
								//System.out.println(tCCond.getTerm() + " is true");
							}
							else if( tokens.getNetState(tCCond.getTerm()) == PetriNetState.WAIT_BUT_ACCEPTED )
								tmpArr[count++] = 0.5;
								
							//System.out.println("Finished checking "+ tCCond.getTerm() + " [CF] in Net:("+net+")");
						}
						//Atomic Proposition 
						else if( tCCond instanceof SimpleCondition ){
							//System.out.println("Starting checking "+ tCCond.getTerm() + " [CS] ");
							if( EntailOperator.getInstance().entailsCondition(state, assumptions, ((SimpleCondition) tCCond).getCondition()) ){
								tmpArr[count++] = 1.0;
								//System.out.println(tCCond.getTerm() + " is true");
							}
							//System.out.println("Finished checking "+ tCCond.getTerm() + " [CS] in Net:("+net+")");
						}
					}
					if( (tmpArr[0] + tmpArr[1]) >= 1.489 )
						fire(t, tokens, net);
					//System.out.println("Finished checking "+ tCond.getTerm() + " [C] in Net:("+net+")");
				}
				//True Condition
				else if( tCond instanceof TrueCondition ){
					//System.out.println("-Net:("+net+")\nStarting checking Empty Condition [T] ");
					fire(t, tokens, net);//Always fires
					//System.out.println("Finished checking Empty Condition [T] in Net:("+net+")");
				}
			}
		}
	}
	
	/**
	 * This method checks if a formula is satisfied by scanning the representing net.
	 *
	 * @param tCond
	 *            the transition condition
	 * @param tokens
	 *            the tokens
	 * @param state
	 *            the state
	 * @param visitedNets
	 *            the visited nets
	 * @return true if the formula is satisfied
	 */
	private boolean formulaCheck(FormulaCondition tCond, TokensConfiguration tokens, StateOfWorld state, HashSet<String> visitedNets) {
		String cNet = tCond.getTerm();
		PetriNetState cNetState = tokens.getNetState(cNet);
		
		//In this case the net representing the formula hasn't been accessed yet 
		if( cNetState == null ){ 
			tokens.addToken(cNet, nets.initNet(cNet)); //So initialize
			cNetState = nets.getNetState(cNet);
			tokens.setNetState(cNet, cNetState);
		}
		//Checks if the formula condition became Accepted or Error
		if( !visitedNets.contains(cNet) )
			superviseNet( tokens, cNet, state, visitedNets );
		
		//System.out.println( "|> Net " + cNet + " is " + tokens.getNetState(cNet) + " and Condition requires " + tCond.getCond() + " |");
		
		return tokens.getNetState(cNet)== tCond.getCond() ;
	}
	
	/**
	 * Used to fire a transition in a Net and update the Tokens Configuration.
	 *
	 * @param t
	 *            the transition
	 * @param tokens
	 *            the tokens
	 * @param net
	 *            the net
	 */
	private void fire( Transition t, TokensConfiguration tokens, String net) {
		for( Arc in : t.getIncoming() )
			tokens.removeToken(net, in.getPlace().getName());
		t.fire();
		for( Arc out : t.getOutgoing() ) //I know for construction that it's just one place
			tokens.addToken(net, out.getPlace().getName());
		tokens.setNetState(net, nets.getNetState(net));
		//System.out.println("Fire "+net + " in State Place  " + nets.getNetState(net));
	}
	
	/**
	 * The score function that elaborates the score for an ExpansionNode.
	 * If expNode has multiple destinations it sets the score to the minimum 
	 *  score obtained by its eNode destinations.   
	 *
	 * @param expNode
	 *            the expansionNode that needs a score
	 */
	private void score( GraphExpansion expNode ) {
		int score = 0;
		for( ExtendedNode destination : expNode.getDestination() ){
			score = max(score, destination.getScore());
		}
		expNode.setScore(score);
	}
	
	/**
	 * Method used to get the highest node to visit.
	 *
	 * @return the highest node to visit
	 */
	private ExtendedNode getHighestNodeToVisit(){
		
		if(toVisit.size() == 0)	return null;
		
		int index = toVisit.size() - 1;
		return toVisit.remove(index);
	}
	
	public boolean toVisitIsEmpty() {
		return toVisit.isEmpty();
	}
	
	/**
	 * Removes the expanded list.
	 */
	public void removeExpandedList(){
		this.expandedList.removeAll(this.expandedList);
	}
	
	/**
	 * This operation is used to set new Capabilities
	 *
	 * @param capabilities
	 *            the new Capabilities List
	 */
	public void setCapabilities(ArrayList<AbstractCapability> capabilities){
		this.capabilities = capabilities;
	}
	
	/**
	 * This operation is used to add a new Capability
	 *
	 * @param capability
	 *            the new Capability
	 */
	public void addCapability(AbstractCapability capability){
		this.capabilities.add(capability);
	}
	
	/**
	 * A standard method for finding the minimum value between two.
	 *
	 * @param a
	 *            the first value
	 * @param b
	 *            the second value
	 * @return the minimum between the two values
	 */
	private int max( int a, int b ){
		if( a >= b ) 
			return a;
		else 
			return b;
	}

	/** 
	 * For Testing 
	 * */
	public ArrayList<GraphExpansion> getExpandedList(){
		return this.expandedList;
	}
	
//	public Set<ExtendedNode> getAllStates(){
//		return this.all_states;
//	}

	public ArrayList<ExtendedNode> getToVisit(){
		return this.toVisit;
	}
	
	public PNHierarchy getNets() {
		return nets;
	}
	
	public void log_current_state() {
		System.out.println("========iteration ("+iterations+")========");
		System.out.println("------------visited states---------------");
		for (StateOfWorld w : visited) {
			System.out.println(w.toString());
		}
		System.out.println("------------to visited states---------------");
		for (ExtendedNode n : toVisit) {
			System.out.print(n.getWorldState().toString());
			if (n.isExitNode())
				System.out.print(" Exit ");
			if (n.isErrorNode())
				System.out.print(" Error ");
			System.out.println("");
		}
		System.out.println("--------------expanded---------------");
		for (GraphExpansion ex : expandedList) {
			for (ExtendedNode n : ex.getDestination() ) {
				System.out.print(ex.getSource().getWorldState().toString()+"->"+ex.getCapability()+"->\t");
				if (n.isExitNode())
					System.out.println(n.getWorldState().toString()+" EXIT =>\t"+ex.hashCode()+" [score("+ex.getScore()+")]");
				else
					System.out.println(n.getWorldState().toString()+"=>\t"+ex.hashCode()+" [score("+ex.getScore()+")]");
			}
		}
	}
	
}
