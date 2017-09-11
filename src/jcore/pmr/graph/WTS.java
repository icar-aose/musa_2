package pmr.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import layer.awareness.AbstractCapability;
import layer.semantic.StateOfWorld;
import pmr.probexp.ENode;
import pmr.probexp.ExpansionNode;
import pmr.probexp.MultipleExpansionNode;
import pmr.probexp.NormalExpansionNode;

/**
 * The Class WTS is the graph of solutions implementation.
 *
 * @author Alessandro Fontana
 */
public class WTS {

	/**
	 * The graph. Use an HashMap as implementation to make the computational cost of
	 * the operations constant
	 */
	// private HashMap<WorldNode, WorldNode> graph;
	private HashMap<String, WorldNode> graph;

	private WorldNode start;

	/**
	 * Instantiates a new wts. The root is a WorldNode with a null StateOfWorld
	 */
	public WTS() {
		this.graph = new HashMap<String, WorldNode>();
	}

	/**
	 * Set the first Node of the graph. It creates a new WorldNode using the
	 * StateOfWorld param.
	 *
	 * @param state
	 *            the first StateOfWorld
	 */
	public void setInitialState(StateOfWorld state) {

		start = this.createSafeNode(state);

	}

	public WorldNode getInitialState() {
		return this.start;
	}

	/**
	 * Adds inside the graph the WorldNodes present in the ExpansionNode. This
	 * method normally adds the WorldNode and the edges inside the graph. It also
	 * generate the OPNode if the ExpansionNode is an instance of
	 * MultipleExpansionNode
	 *
	 * @param newnode
	 *            the new node
	 */
	public void addExpansionNode(ExpansionNode newnode) {
		if (graph.size() == 0) {
			start = createSafeNode(newnode.getSource().getWorldState());
		}
		if (newnode instanceof NormalExpansionNode) {
			/*
			 * it normally adds The WorldNodes in newnode. The destinationList of newnode
			 * has size = 1.
			 */
			NormalExpansionNode tempnode = (NormalExpansionNode) newnode;

			WorldNode source_node = this.createSafeNode(tempnode.getSource().getWorldState());
			WorldNode dest_node = null;

			if (tempnode.getDestination().size() > 0)
				dest_node = this.createSafeNode(tempnode.getDestination().get(0).getWorldState());
			else {
				System.out.println("Warning: source and destination nodes are the same");
				dest_node = source_node;
			}

			this.addEdge(source_node, dest_node, tempnode.getCapability(), tempnode.getAgent());

		} else {
			/*
			 * it normally adds the source node in newnode, then it generates an OPNode that
			 * connect the source with all the nodes in the destinationList of newnode. When
			 * it has added all the nodes in the list, it adds the OPNode inside the source
			 * WorldNode. The destinationList of newnode has size = n. if the source already
			 * contains that OPNode, it makes no actions and exit from the method.
			 */
			MultipleExpansionNode exptempnode = (MultipleExpansionNode) newnode;

			WorldNode source_node = this.createSafeNode(exptempnode.getSource().getWorldState());

			OPNode faketempnode = new XORNode(exptempnode.getCapability(), exptempnode.getScore(),
					exptempnode.getAgent());
			faketempnode.setIncomingEdge(
					new OPEdge(source_node, faketempnode, exptempnode.getCapability(), exptempnode.getAgent()));
			for (ENode etemp : newnode.getDestination()) {
				WorldNode wnode = this.createSafeNode(etemp.getWorldState());
				// System.out.println("adding xor "+source_node.getId()+" to "+wnode.getId());
				faketempnode.addOutcomingEdge(new EvolutionEdge(faketempnode, wnode, exptempnode.getScenario(etemp)));
			}

			source_node.addOPNode(faketempnode);
		}
	}

	public WorldNode createSafeNode(StateOfWorld desc) {
		WorldNode new_node = null;

		if (desc == null) {
			new_node = new WorldNode(new StateOfWorld());
			this.graph.put("init_node", new_node);
		} else {
			if (this.graph.containsKey(desc.toString())) {
				return graph.get(desc.toString());
			} else {
				new_node = new WorldNode(desc);
				this.graph.put(desc.toString(), new_node);
			}
		}

		return new_node;
	}

	// /**
	// * Adds a simple WorldNode. it create a new object to avoid references
	// problem.
	// *
	// * @param source
	// * the source
	// * @return true, if successful
	// */
	// public boolean addSafeNode(WorldNode source){
	// if(this.graph.containsKey(source) == false){
	//
	//// WorldNode temp = new WorldNode(source.getWorldState());
	//// temp.setIncomingEdgeList(source.getIncomingEdgeList());
	//// temp.setOutcomingEdgeList(source.getOutcomingEdgeList());
	//
	// this.graph.put(source.getWorldState().toString(), source);
	// return true;
	// }
	// else
	// return false;
	// }

	/**
	 * Adds a Normaledge from a WorldNode to a WorldNode.
	 *
	 * @param sourcenode
	 *            the source
	 * @param destnode
	 *            the destination
	 * @param capability
	 *            the capability
	 */
	public void addEdge(WorldNode sourcenode, WorldNode destnode, String capability) {
		sourcenode.addOutcomingEdge(new NormalEdge(sourcenode, destnode, capability));
		destnode.addIncomingEdge(new NormalEdge(sourcenode, destnode, capability));
	}

	public void addEdge(WorldNode sourcenode, WorldNode destnode, String capability, String agent) {
		sourcenode.addOutcomingEdge(new NormalEdge(sourcenode, destnode, capability, agent));
		destnode.addIncomingEdge(new NormalEdge(sourcenode, destnode, capability, agent));
	}

	/**
	 * return the Edge to edit if present, null otherwise.
	 *
	 * @param node
	 *            the node
	 * @param capability
	 *            the capability
	 * @return the edge, with the selected capability stored inside the selected
	 *         WorldNode.
	 */
	public Edge editEdge(WorldNode node, AbstractCapability capability) {
		for (NormalEdge tempedge : this.graph.get((Object) node).getOutcomingEdgeList())
			if (tempedge.getCapability().equals(capability.toString()))
				return tempedge;

		for (OPNode tempnode : this.graph.get((Object) node).getOPNodeList())
			if (tempnode.getCapability().equals(capability.toString()))
				return tempnode.getIncomingEdge();

		return null;
	}

	/**
	 * Gets the graph.
	 *
	 * @return the graph of the solutions.
	 */
	public HashMap<String, WorldNode> getWTS() {
		return this.graph;
	}

	/**
	 * Removes a World node.
	 *
	 * @param node
	 *            the node
	 */
	public void removeNode(WorldNode node) {
		NormalEdge Nedge;
		EvolutionEdge Eedge;

		for (Edge temp : node.getIncomingEdgeList()) {
			if (temp instanceof NormalEdge) {
				Nedge = (NormalEdge) temp;
				Nedge.getSource().removeOutcomingEdge(new NormalEdge(Nedge.getSource(), node, Nedge.getCapability()));
			} else {
				Eedge = (EvolutionEdge) temp;
				Eedge.getSource().removeOutcomingEdge(
						new EvolutionEdge(Eedge.getSource(), node, Eedge.getSource().getCapability()));
			}
		}
		this.graph.remove(node.getWorldState().toString());
	}

	/**
	 * Removes the edge from a WorldNode to a WorldNode.
	 *
	 * @param sourcenode
	 *            the sourcenode
	 * @param destnode
	 *            the destnode
	 */
	public void removeEdge(WorldNode sourcenode, WorldNode destnode) {

		if (sourcenode != null && destnode != null) {
			sourcenode.removeOutcomingEdge(new NormalEdge(sourcenode, destnode, null));
			destnode.removeOutcomingEdge(new NormalEdge(sourcenode, destnode, null));
		}
		//
		// if(this.graph.containsKey(sourcenode) == true &&
		// this.graph.containsKey(destnode) == true){
		// WorldNode tempnode = (WorldNode) destnode;
		// this.graph.get(sourcenode).removeOutcomingEdge(new NormalEdge(sourcenode,
		// tempnode, null));
		// this.graph.get(tempnode).removeIncomingEdge(new NormalEdge(sourcenode,
		// tempnode, null));
		// }
	}

	/**
	 * Removes the edge from a WorldNode to an OPNode. This edge deletion is
	 * performed deleting the OPNode from the OPNodeList inside the WorldNode. It
	 * returns true if the OPNode was found, false otherwise.
	 *
	 * @param sourcenode
	 *            the sourcenode
	 * @param destnode
	 *            the destnode
	 * @return true, if the edge was present and removed.
	 */
	public boolean removeEdge(WorldNode sourcenode, OPNode destnode) {
		if (sourcenode != null && destnode != null) {
			sourcenode.removeOPNode(destnode);
			return true;
		}
		// if(this.graph.containsKey(sourcenode) == true &&
		// this.graph.containsKey(destnode) == true){
		// this.graph.get(sourcenode).removeOPNode(destnode);
		// return true;
		// }
		return false;
	}

	/**
	 * Gets the solution paths present inside the graph. It performs a visit inside
	 * the graph and stores the solutions inside an ArrayList of ArrayList of
	 * WorldNode, returning this structure when the visit is over.
	 *
	 * @param start
	 *            the start
	 * @param exitNodeMap
	 *            the exit node map
	 * @return the list of solution paths.
	 */
	public ArrayList<ArrayList<WorldNode>> getSolutions(WorldNode start, HashMap<WorldNode, WorldNode> exitNodeMap) {
		ArrayList<ArrayList<WorldNode>> result = new ArrayList<>();
		ArrayList<WorldNode> pathNode = new ArrayList<WorldNode>();
		HashMap<WorldNode, Integer> checkedNode = new HashMap<WorldNode, Integer>();
		WTSVisit(start, exitNodeMap, result, pathNode, checkedNode);
		return result;
	}

	/**
	 * WTS visit. This algorithm perform a revisited DFS, storing all the possible
	 * solutions inside the graph. The first paths checked are the normal paths from
	 * a WorldNode to a WorldNode, then it analyze every OPNode stored inside the
	 * WorldNodes.
	 *
	 * @param start
	 *            the start
	 * @param exitNodeMap
	 *            the exit node map
	 * @param result
	 *            the result
	 * @param pathNode
	 *            the path node
	 * @param checkedNode
	 *            the checked node
	 */
	private void WTSVisit(WorldNode start, HashMap<WorldNode, WorldNode> exitNodeMap,
			ArrayList<ArrayList<WorldNode>> result, ArrayList<WorldNode> pathNode,
			HashMap<WorldNode, Integer> checkedNode) {

		/*
		 * if the current start node is an exit node, it is stored in the path and the
		 * path is stored in the result. Then the start is removed from the path.
		 */
		if (exitNodeMap.containsKey(start) == true) {
			pathNode.add(start);
			ArrayList<WorldNode> temp = new ArrayList<>(pathNode);
			result.add(temp);
			pathNode.remove(start);
			return;
		}

		/*
		 * the first cycle. It check every single NormalEdge of every single not
		 * solution WorldNodes present in the graph.
		 */
		for (NormalEdge edge : this.graph.get((Object)start).getOutcomingEdgeList()) {
			if (checkedNode.containsKey(start) == false) {
				pathNode.add(start);
				checkedNode.put(start, 1);
				WTSVisit(edge.getDestination(), exitNodeMap, result, pathNode, checkedNode);
				pathNode.remove(start);
			}
		}

		/* if no more NormalEdges are present, it starts to analyze the OPNodelist and
		 * every OPEdge of each OPNode inside the list */
		for (OPNode node : this.graph.get((Object)start).getOPNodeList()) {
			for (int j = 0; j < node.getOutcomingEdge().size(); j++) {
				if (checkedNode.containsKey(node.getOutcomingEdge().get(j).getDestination()) == false) {
					pathNode.add(node.getOutcomingEdge().get(j).getDestination());
					checkedNode.put(start, 1);
					WTSVisit(node.getOutcomingEdge().get(j).getDestination(), exitNodeMap, result, pathNode,
							checkedNode);
					pathNode.remove(node.getOutcomingEdge().get(j).getDestination());
				}
			}
		}
		checkedNode.remove(start);
	}

	public void printGraph() {
		Iterator<String> i = this.graph.keySet().iterator();
		System.out.println("\n graphviz\n \n");
		while (i.hasNext()) {
			String temp = (String) i.next();

			WorldNode w = this.graph.get(temp);

			for (NormalEdge e : w.getOutcomingEdgeList()) {
				System.out.println("Node" + w.getId() + " -> " + "Node" + e.getDestination().getId() + "[label=\""
						+ e.getCapability() + "\"]");
			}

			for (OPNode opNode : w.getOPNodeList()) {
				for (EvolutionEdge ee : opNode.getOutcomingEdge()) {
					System.out.println("Node" + w.getId() + " -> " + "Node" + ee.getDestination().getId() + " [label=\""
							+ ee.getScenario() + "\"][style=bold][color=red]");
				}
			}
		}
	}

}
