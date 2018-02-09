package org.icar.testcase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RemoveAnyPredicate_test.class, AnswerSetTest.class, DLPHeadHashSet_Test.class, DomainEntailParamTest.class, DomainEntailTest1.class,
		DomainEntailTest2.class, EdgeTest.class, ExpansionTranslatorTest.class, ExtendedNodeTranslation_Test.class,
		GoalModelTest.class, NetTest.class, ProvaSPSDomain_Test.class, SequencesTest.class, 
		SPSREasyTest.class, StateOfWorldTest.class, LTL_Globally_Finally_Test.class })
public class AllTests {

}
