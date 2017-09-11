package configuration.distributed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Mirko Avantaggiato
 *
 */
public class Sequences {
	private String firstStateName;
	private ArrayList<ArrayList<String>> seqs;
	private HashSet<String> nodes;
	private HashSet<String> leafNodes;
	/* Helper for findAllPaths */
	private ArrayList<ArrayList<String>> allPaths;
	/**/
	private HashSet<String> successNodes;
	private HashSet<String> treeSafeNodes;
	private SolutionSet solutionsSoFar;

	public class Triple {
		private String a;
		private String b;
		private String c;

		Triple(String a, String b, String c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public boolean equals(Object obj) {
			Triple that = (Triple) obj;
			if (this.a.equals(that.a))
				if (this.b.equals(that.b))
					if (this.c.equals(that.c))
						return true;
			return false;
		}

		@Override
		public String toString() {
			return "[" + this.a + ", " + this.b + ", " + this.c + "]";
		}

	}

	private ArrayList<Triple> capabilities;

	public Sequences() {
		this.seqs = new ArrayList<>();
		this.nodes = new HashSet<>();
		this.leafNodes = new HashSet<>();
		this.allPaths = new ArrayList<>();
		this.successNodes = new HashSet<>();
		this.treeSafeNodes = new HashSet<>();
		this.solutionsSoFar = new SolutionSet(this);
		this.capabilities = new ArrayList<>();
	}

	public void processEdge(String src, String dest, String capability) {
		if (!this.capabilities.contains(new Triple(src, dest, capability)))
			this.capabilities.add(new Triple(src, dest, capability));
		/* temporaneo */
		src = src.replace("\n", "");
		dest = dest.replace("\n", "");
		/* ----- */
		ArrayList<String> edgeToConsider = new ArrayList<>();
		edgeToConsider.add(src);
		edgeToConsider.add(dest);
		/*
		 * First edge.
		 */
		if (!this.nodes.contains(src) && !this.nodes.contains(dest) && this.seqs.size() == 0) {
			// System.out.println("Primo arco.");
			ArrayList<String> tmp = new ArrayList<>();
			tmp.add(src);
			tmp.add(dest);
			this.seqs.add(tmp);
			this.nodes.add(src);
			this.nodes.add(dest);
			this.leafNodes.add(dest);
			this.firstStateName = src;
		}
		/*
		 * Se src � nei nodi ma dest no, allora � un arco verso un nuovo nodo.
		 */
		else if (this.nodes.contains(src) && !this.nodes.contains(dest)) {
			this.nodes.add(dest);
			this.leafNodes.add(dest);
			this.leafNodes.remove(src);
			ArrayList<ArrayList<String>> notLastNode = new ArrayList<>();
			for (ArrayList<String> seq : this.seqs)
				if (!seq.get(seq.size() - 1).equals(src) && seq.contains(src))
					notLastNode.add(new ArrayList<>(seq));
			for (int i = 0; i < notLastNode.size(); i++)
				notLastNode.set(i, arrayTruncateFromTo(notLastNode.get(i), 0, notLastNode.get(i).indexOf(src) + 1));
			for (ArrayList<String> seq : notLastNode)
				if (!this.seqs.contains(seq))
					this.seqs.add(new ArrayList<>(seq));
			for (ArrayList<String> seq : this.seqs)
				if (seq.get(seq.size() - 1).equals(src))
					seq.add(dest);
		}
		/*
		 * Se entrambi nodi sono gi� noti, bisogna fare attenzione.
		 */
		else if (this.nodes.contains(src) && this.nodes.contains(dest)) {
			this.leafNodes.remove(src);
			// System.out.println("Edge to consider: " +
			// edgeToConsider);
			if (whereCanIGoFrom(dest, seqs, edgeToConsider).size() == 0)
				this.leafNodes.add(dest);
			else
				this.leafNodes.remove(dest);
			ArrayList<ArrayList<String>> involvedSeqs = new ArrayList<ArrayList<String>>();
			for (ArrayList<String> s : this.seqs)
				if ((s.contains(src) || s.contains(dest)))
					involvedSeqs.add(new ArrayList<>(s));
			for (ArrayList<String> s : involvedSeqs) {
				// System.out.println("Considering seq:" + s);
				/*
				 * Sia src sia dest sono nella stessa seq.
				 */
				if (s.contains(src) && s.contains(dest)) {
					/*
					 * a -> b e la seq. � del tipo 1 2 3 a 4 5 b
					 */
					if (s.indexOf(src) < s.indexOf(dest)) {
						// System.out.println("Caso index(src) <
						// index(dest)");
						ArrayList<String> leftSide = arrayTruncateFromTo(s, 0, s.indexOf(src) + 1);
						// System.out.println("Leftside: " + leftSide);
						this.allPaths.clear();
						// System.out.println(this.leafNodes);
						findAllPaths(dest, this.leafNodes, this.seqs, new ArrayList<>(), edgeToConsider);
						ArrayList<ArrayList<String>> rightSide = new ArrayList<>(this.allPaths);
						// System.out.println(rightSide);
						for (int i = 0; i < rightSide.size(); i++)
							rightSide.set(i, arrayTillEnd(rightSide.get(i), rightSide.get(i).indexOf(dest)));
						// System.out.println("Rightside: " +
						// rightSide);
						for (ArrayList<String> rs : rightSide)
							if (!this.seqs.contains(arrayJoin(leftSide, rs)))
								this.seqs.add(arrayJoin(leftSide, rs));
					}
					/*
					 * b -> a e la seq. � del tipo 1 2 3 a 4 5 b
					 */
					else if (s.indexOf(src) > s.indexOf(dest)) {
						// System.out.println("Caso index(src) >
						// index(dest)");
						if (s.get(s.size() - 1).equals(src)) {
							// System.out.println("Adding loop
							// at the end");
							this.seqs.remove(s);
							s.add(dest + "*");
							if (!seqs.contains(s))
								this.seqs.add(s);
						} else {
							ArrayList<String> newL = arrayTruncateFromTo(s, 0, s.indexOf(src) + 1);
							newL.add(dest + "*");
							if (!this.seqs.contains(newL))
								this.seqs.add(newL);
						}
					}
				} else {
					HashSet<String> srcSet = new HashSet<>(1);
					srcSet.add(src);
					this.allPaths.clear();
					findAllPaths(this.firstStateName, srcSet, involvedSeqs, new ArrayList<>(), edgeToConsider);
					ArrayList<ArrayList<String>> leftSide = new ArrayList<>(this.allPaths);
					for (int i = 0; i < leftSide.size(); i++)
						this.allPaths.set(i, arrayTruncateFromTo(leftSide.get(i), 0, leftSide.get(i).size() - 1));
					this.allPaths.clear();
					findAllPaths(dest, this.leafNodes, involvedSeqs, new ArrayList<>(), edgeToConsider);
					ArrayList<ArrayList<String>> rightSide = new ArrayList<>(this.allPaths);
					for (int i = 0; i < rightSide.size(); i++)
						this.allPaths.set(i, arrayTillEnd(rightSide.get(i), rightSide.get(i).indexOf(dest)));
					for (ArrayList<String> _ls : leftSide)
						for (ArrayList<String> _rs : rightSide) {
							ArrayList<String> tmp = arrayJoin(_ls, _rs);
							if (!seqs.contains(tmp))
								seqs.add(tmp);
						}
				}
			}
		} else {
			;// pass
		}
		Iterator<ArrayList<String>> i = this.seqs.iterator();
		while (i.hasNext()) {
			ArrayList<String> tmp = i.next();
			if (!this.leafNodes.contains(tmp.get(tmp.size() - 1)) && !tmp.get(tmp.size() - 1).contains("*"))
				i.remove();
		}
	}

	public void processSolution(String solutionNode) {
		/* temporaneo */
		solutionNode = solutionNode.replace("\n", "");
		/* ----- */
		this.successNodes.add(solutionNode);

		Tree<String> root = new Tree<String>(this.seqs.get(0).get(0));
		root.setNodeType(Tree.NORMAL_CODE);
		for (ArrayList<String> seq : this.seqs) {
			Tree<String> currentNode = root;
			Iterator<String> i = seq.iterator();
			if (i.hasNext())
				i.next();
			while (i.hasNext()) {
				Tree<String> newBorn = new Tree<String>((String) i.next());
				if (this.successNodes.contains(newBorn.getValue()))
					newBorn.setNodeType(Tree.SUCCESS_CODE);
				else if (newBorn.getValue().contains("X"))
					newBorn.setNodeType(Tree.EXPLICIT_XOR_CODE);
				else if (newBorn.getValue().contains("*"))
					newBorn.setNodeType(Tree.LOOP_CODE);
				else if (i.hasNext())
					newBorn.setNodeType(Tree.NORMAL_CODE);
				else
					newBorn.setNodeType(Tree.FAILURE_CODE);
				currentNode.addChild(newBorn);
				currentNode = currentNode.getChild(newBorn);
			}
		}
		// System.out.println("Tree:");
		// printTree(root, 0);
		// System.out.println("");
		ArrayList<Tree<String>> tmp = tree_toSolutionSet(root);
		// int j = 0;
		if (tmp != null)
			if (tmp != null) {
				for (Tree<String> sol : tmp) {
					Solution _sol = new Solution(sol, this.solutionsSoFar);
					if (!this.solutionsSoFar.containsSolution(_sol))
						this.solutionsSoFar.addSolution(_sol);
				}
				Iterator<Solution> i = this.solutionsSoFar.iterator();
				while (i.hasNext()) {
					Solution s = i.next();
					this.makeTreeSafe(s.getRoot());
					/*
					 * removing a solutions if it has got a loop which does not
					 * arrive to a safe Tree node or if it is a simple path
					 * ending with a loop (like A->B->C->L(A))
					 */
					// System.out.println("s.checkLoop: " + s.checkLoop());
					// System.out.println("s.isSimplePath: " +
					// s.isSimplePath());
					// System.out.println("s.containsLoop: " +
					// s.containsLoop());
					// System.out.println("Safe nodes: " +
					// this.getTreeSafeNodes());
					// System.out.println("Checking solution:");
					// printTree(s.getRoot(), 0);
					if (s.checkLoop() == false || (s.isSimplePath() == true && s.containsLoop() == true))
						i.remove();
				}
			}
		int i = 0;
		for (Solution s : this.solutionsSoFar) {
			System.out.println("Solution " + i++);
			printTree(s.getRoot(), 0);
		}
	}

	private ArrayList<Tree<String>> tree_toSolutionSet(Tree<String> currentNode) {
		ArrayList<Tree<String>> solutions = new ArrayList<>();
		if (currentNode.isLeaf()) {
			Tree<String> lastNode = new Tree<String>(currentNode.getValue());
			lastNode.setNodeType(currentNode.getNodeType());
			if (currentNode.getNodeType() == Tree.SUCCESS_CODE || currentNode.getNodeType() == Tree.LOOP_CODE) {
				solutions.add(lastNode);
				return solutions;
			} else
				return null;
		} else {
			if (currentNode.getNodeType() == Tree.XOR_CODE || currentNode.getNodeType() == Tree.NORMAL_CODE) {
				for (Tree<String> child : currentNode.getChildren()) {
					ArrayList<Tree<String>> subSolutions = tree_toSolutionSet(child);
					if (subSolutions != null)
						for (Tree<String> solutionTree : subSolutions) {
							Tree<String> subTree = new Tree<String>(currentNode.getValue());
							subTree.setNodeType(currentNode.getNodeType());
							subTree.getChildren().add(solutionTree);
							solutions.add(subTree);
						}
				}
				if (solutions.isEmpty())
					return null;
				else
					return solutions;
			} else if (currentNode.getNodeType() == Tree.EXPLICIT_XOR_CODE) {
				HashSet<ArrayList<Tree<String>>> set = new HashSet<>();
				for (Tree<String> child : currentNode.getChildren()) {
					ArrayList<Tree<String>> subSolutions = tree_toSolutionSet(child);
					if (subSolutions != null)
						set.add(subSolutions);
					else
						return null;
				}
				HashSet<ArrayList<String>> set_copy = new HashSet<>();
				for (ArrayList<Tree<String>> al : set) {
					ArrayList<String> toAdd = new ArrayList<>();
					for (Tree<String> ts : al)
						toAdd.add(ts.getID_asString());
					set_copy.add(toAdd);
				}
				Iterator<ArrayList<String>> set_copyIterator = set_copy.iterator();
				if (set_copyIterator.hasNext()) {
					ArrayList<String> first = (ArrayList<String>) set_copyIterator.next();
					while (set_copyIterator.hasNext()) {
						ArrayList<String> first_copy = new ArrayList<>(first);
						ArrayList<String> second = (ArrayList<String>) set_copyIterator.next();
						first = Sequences.combineTwoArrayList(first_copy, second);
					}
					for (String examinedString : first) {
						Tree<String> subTree = new Tree<String>(currentNode.getValue());
						subTree.setNodeType(currentNode.getNodeType());
						String[] S = examinedString.split("@");
						ArrayList<String> stringsToParse = new ArrayList<>();
						for (int i = 0; i < S.length; i++)
							stringsToParse.add(S[i]);
						for (String s : stringsToParse) {
							Tree<String> toAddAsChild = new Tree<String>("");
							for (ArrayList<Tree<String>> array : set)
								for (Tree<String> tree : array)
									if (tree.getID_asString().equals(s)) {
										toAddAsChild.setValue(tree.getValue());
										toAddAsChild.setNodeType(tree.getNodeType());
										toAddAsChild.setChildren(tree.getChildren());
										subTree.getChildren().add(Tree.getSameTree_newID(toAddAsChild));
									}
						}
						solutions.add(subTree);
					}
				}
				return solutions;
			}
			return solutions;
		}
	}

	/**
	 * This method combines 2 ArrayLists<String>. <br>
	 * Example: A = [a, b, c] B = [d, e] <br>
	 *
	 * <b>res = [ad, ae, bd, be, cd, ce]</b>
	 *
	 */
	private static ArrayList<String> combineTwoArrayList(ArrayList<String> A, ArrayList<String> B) {
		ArrayList<String> res = new ArrayList<>();
		for (String a : A)
			for (String b : B)
				res.add(a + "@" + b);
		return res;
	}

	public ArrayList<ArrayList<String>> getSeqs() {
		return seqs;
	}

	public static void printTree(Tree<String> node, int depth) {
		if (node != null) {
			String tab = String.join("", Collections.nCopies(depth, "  "));
			System.out.println(tab + node.getValue() + " (" + Tree.typeToString(node.getNodeType()) + ")");
			for (Tree<String> t : node.getChildren())
				printTree(t, depth + 1);
		}
	}

	/**
	 * from incluso, to escluso.
	 *
	 * @param l
	 * @param from
	 * @param to
	 * @return
	 */
	private static ArrayList<String> arrayTruncateFromTo(ArrayList<String> l, int from, int to) {
		ArrayList<String> tmp = new ArrayList<>();
		int c = from;
		while (to - c != 0)
			tmp.add(l.get(c++));
		return tmp;
	}

	private static ArrayList<String> arrayTillEnd(ArrayList<String> l, int start) {
		ArrayList<String> tmp = new ArrayList<>();
		for (int c = start; c < l.size(); c++)
			tmp.add(l.get(c));
		return tmp;
	}

	/**
	 * This method joins two arrays.
	 *
	 * @param a1
	 * @param a2
	 * @return
	 */
	private static ArrayList<String> arrayJoin(ArrayList<String> a1, ArrayList<String> a2) {
		ArrayList<String> tmp = new ArrayList<>();
		for (String s : a1)
			tmp.add(s);
		if (tmp.get(tmp.size() - 1).contains("*"))
			return tmp;
		for (String s : a2) {
			if (s.contains("*")) {
				String[] t = s.split("\\*");
				if (s.contains(t[0]))
					tmp.add(s);
				return tmp;
			}
			if (tmp.contains(s) == false)
				tmp.add(s);
			else {
				tmp.add(s + "*");
				return tmp;
			}
		}
		return tmp;
	}

	private static HashSet<String> whereCanIGoFrom(String vertex, ArrayList<ArrayList<String>> seqs,
			ArrayList<String> edgeToConsider) {
		HashSet<String> nodes = new HashSet<>();
		if (edgeToConsider != null && edgeToConsider.get(0) == vertex)
			nodes.add(edgeToConsider.get(1));
		ArrayList<ArrayList<String>> tmp = new ArrayList<>();
		for (ArrayList<String> t : seqs)
			if (t.contains(vertex) && !t.get(t.size() - 1).equals(vertex))
				tmp.add(new ArrayList<String>(t));
		String toAdd;
		for (ArrayList<String> t : tmp) {
			toAdd = t.get(t.indexOf(vertex) + 1);
			if (!toAdd.contains("*"))
				nodes.add(toAdd);
			else
				nodes.add(toAdd.split("\\*")[0]);
		}
		return nodes;
	}

	private void findAllPaths(String s, HashSet<String> destinations, ArrayList<ArrayList<String>> seqs,
			ArrayList<String> path, ArrayList<String> edgeToConsider) {
		path.add(s);
		if (destinations.contains(s))
			this.allPaths.add(new ArrayList<String>(path));
		else {
			for (String node : whereCanIGoFrom(s, seqs, edgeToConsider))
				if (path.contains(node) == false)
					findAllPaths(node, destinations, seqs, path, edgeToConsider);
				else {
					String loopNode = new String(node + "*");
					path.add(loopNode);
					this.allPaths.add(new ArrayList<String>(path));
					path.remove(path.size() - 1);
				}
		}
		path.remove(path.size() - 1);
	}

	public HashSet<String> getSuccessNodes() {
		return successNodes;
	}

	public HashSet<String> getTreeSafeNodes() {
		return treeSafeNodes;
	}

	/**
	 * A node is safe if it is a success node or if at least one of its children
	 * is safe. Bottom-up approach.
	 *
	 * @param t
	 */
	private void makeTreeSafe(Tree<String> t) {
		if (t != null) {
			if (t.getNodeType() == Tree.SUCCESS_CODE) {
				// System.out.println("Adding " + t.getValue() + " to " +
				// this.treeSafeNodes);
				this.treeSafeNodes.add(t.getValue());
			} else
				for (Tree<String> child : t.getChildren()) {
					// System.out.println("Calling on " + child.getValue());
					this.makeTreeSafe(child);
					if (this.treeSafeNodes.contains(child.getValue())
							|| this.treeSafeNodes.contains(child.getValue() + "*")) {
						// System.out.println("Adding " + t.getValue() + "
						// to "
						// + this.treeSafeNodes);
						this.treeSafeNodes.add(t.getValue());
					}
				}
		}
	}

	public SolutionSet getSolutionsSoFar() {
		return solutionsSoFar;
	}

	public void setSolutionsSoFar(SolutionSet solutionsSoFar) {
		this.solutionsSoFar = solutionsSoFar;
	}

	public ArrayList<Triple> getCapabilities() {
		return capabilities;
	}
}
