package pmrtest.translate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import jason.asSyntax.Term;
import layer.awareness.AbstractCapability;
import layer.semantic.StateOfWorld;
import net.sf.tweety.lp.asp.parser.ParseException;
import net.sf.tweety.lp.asp.syntax.DLPHead;
import pmr.graph.WorldNode;
import pmr.probexp.ENode;
import pmr.probexp.ExpansionNode;
import pmr.probexp.MultipleExpansionNode;
import pmr.probexp.NormalExpansionNode;
import translator.JasonDLPHead;
import translator.JasonExpansionNode;
import translator.JasonStateOfWorld;
import translator.TranslateError;

@SuppressWarnings("unused")
public class ExpansionTranslatorTest {

		private StateOfWorld w1;
		private StateOfWorld w2;
		private StateOfWorld w3;
		private StateOfWorld w4;
		private StateOfWorld w5;
		private StateOfWorld w6;
		private StateOfWorld w7;

		private WorldNode n1;
		private WorldNode n2;
		private WorldNode n3;
		private WorldNode n4;
		private WorldNode n5;
		private WorldNode n6;

		private ENode e1;
		private ENode e2;
		private ENode e3;
		private ENode e4;
		private ENode e5;
		private ENode e6;

		private ExpansionNode ex0;
		private ExpansionNode ex1;
		private ExpansionNode ex2;
		private ExpansionNode ex3;
		private ExpansionNode ex4;
		private ExpansionNode ex5;
		private ExpansionNode ex6;
		private ExpansionNode ex7;

		private ExpansionNode ex1Repeat;

		private AbstractCapability cap1;
		private AbstractCapability cap2;
		private AbstractCapability cap3;

	@Before
	public void setUp(){
		this.cap1 = new AbstractCapability("uno",null,null,null);
		this.cap2 = new AbstractCapability("due",null,null,null);
		this.cap3 = new AbstractCapability("tre",null,null,null);

		this.w1 = new StateOfWorld();
		try {
			w1.addFact_asString("penguin(tweety).");
			w1.addFact_asString("penguin2(tweety).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w2 = new StateOfWorld();
		try {
			w2.addFact_asString("penguin(tweety).");
			w2.addFact_asString("penguin(tweety).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w3 = new StateOfWorld();
		try {
			w3.addFact_asString("penguin(tweety).");
			w3.addFact_asString("parrot(polly).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w4 = new StateOfWorld();
		try {
			w4.addFact_asString("sparrow(sid).");
			w4.addFact_asString("broken_wing(sid).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w5 = new StateOfWorld();
		try {
			w5.addFact_asString("Eagle(berry).");
			w5.addFact_asString("can_fly(berry).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w6 = new StateOfWorld();
		try {
			w6.addFact_asString("Eagle(sid).");
			w6.addFact_asString("broken_wing(sid).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}
		this.w7 = new StateOfWorld();
		try {
			w7.addFact_asString("sparrow(claire).");
			w7.addFact_asString("can_fly(claire).");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
			e.printStackTrace();
		}


		this.n1 = new WorldNode(w1);
		this.n2 = new WorldNode(w2);
		this.n3 = new WorldNode(w3);
		this.n4 = new WorldNode(w4);
		this.n5 = new WorldNode(w5);
		this.n6 = new WorldNode(w6);

		this.e1 = new ENode(w1);
		this.e2 = new ENode(w2);
		this.e3 = new ENode(w3);
		this.e4 = new ENode(w4);
		this.e5 = new ENode(w5);
		this.e6 = new ENode(w6);

		ArrayList<ENode> ENodeList345 = new ArrayList<ENode>();
		ENodeList345.add(e3);
		ENodeList345.add(e4);
		ENodeList345.add(e5);

		ArrayList<ENode> ENodeList1 = new ArrayList<ENode>();
		ENodeList1.add(e1);

		ArrayList<ENode> ENodeList2 = new ArrayList<ENode>();
		ENodeList2.add(e2);

		ArrayList<ENode> ENodeList3 = new ArrayList<ENode>();
		ENodeList3.add(e3);

		ArrayList<ENode> ENodeList4 = new ArrayList<ENode>();
		ENodeList4.add(e4);

		ArrayList<ENode> ENodeList5 = new ArrayList<ENode>();
		ENodeList5.add(e5);

		ArrayList<ENode> ENodeList6 = new ArrayList<ENode>();
		ENodeList6.add(e6);

		this.ex0 = new NormalExpansionNode(new ENode(null), ENodeList1, cap1.getId(), "ag1");
		this.ex1 = new MultipleExpansionNode(e1, ENodeList345, cap1.getId(), "ag2");
		this.ex2 = new NormalExpansionNode(e2, ENodeList3, cap2.getId(), "ag3");
		this.ex3 = new NormalExpansionNode(e3, ENodeList6, cap3.getId(), "ag4");
		this.ex4 = new NormalExpansionNode(e4, ENodeList5, cap1.getId(), "ag5");
		this.ex5 = new MultipleExpansionNode(e5, ENodeList345, cap1.getId(), "ag6");
		this.ex6 = new NormalExpansionNode(e6, ENodeList3, cap1.getId(), "ag7");
		this.ex7 = new NormalExpansionNode(e6, ENodeList4, cap2.getId(), "ag8");

	}


	@Test
	public void test() throws TranslateError{
		Term term = JasonExpansionNode.object_to_term(this.ex1);
		ExpansionNode temp = JasonExpansionNode.term_to_object(term);
		System.out.println(this.ex1.getSource().getWorldState());
		System.out.println(temp.getSource().getWorldState());
		assertEquals(temp.getScore(), this.ex1.getScore());
		assertEquals(temp.getDestination().size(), this.ex1.getDestination().size());
		assertEquals(temp.getSource().getWorldState().toString(), this.ex1.getSource().getWorldState().toString());
	}

	@Test
	public void testSOW() throws TranslateError{
		Term term = JasonStateOfWorld.object_to_term(w1);
		StateOfWorld temp = JasonStateOfWorld.term_to_object(term);
		System.out.println(temp);
		System.out.println(w1);
		boolean flag = temp.equals(w1);
		assertEquals(true, flag);
		}


	@Test
	public void testDLP() throws TranslateError{
		DLPHead t1 = this.w1.getFactsList().get(0);
		DLPHead t2 = this.w1.getFactsList().get(1);
		Term term1 = JasonDLPHead.object_to_term(w1.getFactsList().get(0));
		Term term2 = JasonDLPHead.object_to_term(w1.getFactsList().get(1));
		DLPHead tfinal1 = JasonDLPHead.term_to_object(term1);
		DLPHead tfinal2 = JasonDLPHead.term_to_object(term2);
		assertEquals(t1, tfinal1);
		assertEquals(t2, tfinal2);
		}


}
