package layertest.awareness.net;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import layer.awareness.Goal;
import layer.awareness.goalmodel.GoalTreeModel;

import java.util.ArrayList;

/**
 * The TestCase for GoalModel.
 */
public class GoalModelTest {

	/** The model. */
	private GoalTreeModel model;

	/**
	 * Initializing the test
	 */
	@Before
	public void init() {
		Goal g = new Goal("root", null, null);
		model = new GoalTreeModel(g);
	}

	/**
	 * Testing root presence
	 */
	@Test
	public void testRoot() {
		Goal h = new Goal("root", null, null);
		assertEquals(model.getRoot(), h);
	}

	/**
	 * Testing if it doesn't add any arc (the goal named root in the model is already in there,
	 * so a new goal shouldn't be added and an arc to itself neither)
	 */
	@Test
	public void testSameRoot() {
		ArrayList<Goal> a = new ArrayList<>();
		a.add(new Goal("root", null, null));	//New goal but already present in the model (Same name)
		model.addOrArcs(model.getRoot(), a);
		assertNull( model.getArcs(model.getRoot()));
	}

	/**
	 * Testing if it adds a new Arc
	 */
	@Test
	public void testAddArc() {
		ArrayList<Goal> a = new ArrayList<>();
		Goal g = new Goal("second", null, null);
		a.add(g);
		model.addOrArcs(model.getRoot(), a);
		assertEquals( model.getArcs(model.getRoot()).get(0).getNextNode(), g);
	}

	/**
	 * Testing if it doesn't add the same Goal (equal by name) twice
	 */
	@Test
	public void testSameGoals() {
		ArrayList<Goal> a = new ArrayList<>();
		Goal g = new Goal("second", null, null);	// These goals are considered
		Goal h = new Goal("second", null, null);	// equal
		a.add(g);
		a.add(h);
		model.addOrArcs(model.getRoot(), a);
		assertEquals( model.getArcs(model.getRoot()).size(), 1);
	}

}
