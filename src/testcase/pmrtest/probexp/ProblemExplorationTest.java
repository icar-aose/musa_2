package pmrtest.probexp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import layer.awareness.AbstractCapability;
import layer.awareness.Goal;
import layer.awareness.goalmodel.GoalModel;
import layer.awareness.net.Net;
import layer.awareness.net.Token;
import layer.semantic.AssumptionSet;
import layer.semantic.Condition;
import layer.semantic.StateOfWorld;
import layer.semantic.evolution.AddStatement;
import layer.semantic.evolution.CapabilityEvolutionScenario;
import layer.semantic.evolution.EvolutionScenario;
import layer.semantic.evolution.RemoveStatement;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.syntax.Conjunction;
import net.sf.tweety.logics.fol.syntax.ExistsQuantifiedFormula;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.Negation;
import net.sf.tweety.lp.asp.parser.ParseException;
import net.sf.tweety.lp.asp.syntax.DLPAtom;
import net.sf.tweety.lp.asp.syntax.DLPHead;
import pmr.graph.WorldNode;
import pmr.graph.XORNode;
import pmr.probexp.ENode;
import pmr.probexp.ExpansionNode;
import pmr.probexp.MultipleExpansionNode;
import pmr.probexp.NormalExpansionNode;
import pmr.probexp.ProblemExploration;

public class ProblemExplorationTest {

	private ProblemExploration exploration;
	
	private StateOfWorld wStart;
	private StateOfWorld startNode;
	private StateOfWorld regAndCloud;
	private StateOfWorld regNoCloud;
	private StateOfWorld known;
	private StateOfWorld unknown;
	
	private WorldNode nodewStart;
	private WorldNode noderegAndCloud;
	private WorldNode noderegNoCloud;
	private WorldNode nodeknown;
	private WorldNode nodeunknown;
	
	private ENode eStart;
	
	private AbstractCapability NSM;
	private AbstractCapability SFL;
	private AbstractCapability UOPCS;
	private AbstractCapability UOUCS;
	private AbstractCapability GI;
	private AbstractCapability NSF;
	private AbstractCapability CS;
	private AbstractCapability WUD;
	private AbstractCapability SRF;
	private AbstractCapability CU;
	
	private ArrayList<AbstractCapability> capList;
	
	
	private Goal TNF;
	private Goal TDO;
	private Goal TNI;
	private Goal TPAO;
	private Goal TPO;
	private Goal TWO;
	private Goal THO;
	
	private GoalModel model;
	private ArrayList<Token> startTokens;
	
@Before
public void setUp(){
	
	
	AssumptionSet domain = new AssumptionSet();	 
	try {

		domain.addAssumption_asString("role(X) :- user(X).");
		domain.addAssumption_asString("role(X) :- storehouse_manager(X).");
		domain.addAssumption_asString("document(X) :- order(X).");
		domain.addAssumption_asString("document(X) :- invoice(X).");
		domain.addAssumption_asString("document(X) :- user_data(X).");
		domain.addAssumption_asString("document(X) :- registration_form(X).");
		domain.addAssumption_asString("order(X) :- delivery_order(X).");
		domain.addAssumption_asString("processed(X) :- accepted(X), order(X), sent(Y,Z), delivery_order(Y), storehouse_manager(Z).");
		domain.addAssumption_asString("processed(X) :- refused(X), order(X), sent(failure_order,Y), user(Y).");
		domain.addAssumption_asString("notified(X,Y) :- uploaded_on_cloud(X), document(X), has_cloud_space(Y), user(Y).");
		domain.addAssumption_asString("notified(X,Y) :- mailed_perm_link(X,Y), document(X), user(Y).");
	
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAnAssumptionSet e) {
		e.printStackTrace();
	}
	
	/*************/

	Variable doc = new Variable("Doc");
	Variable usr = new Variable("Usr");
	Variable mng = new Variable("Mng");
	
	/*to_handle_order*/
	FOLAtom THO_received = new FOLAtom( new Predicate("received",2));
	THO_received.addArgument(doc);
	THO_received.addArgument(usr);
	FOLAtom THO_order = new FOLAtom( new Predicate("order",1));
	THO_order.addArgument(doc);
	FOLAtom THO_user = new FOLAtom( new Predicate("user",1));
	THO_user.addArgument(usr);
	Condition THO_tc = new Condition( new Conjunction(THO_received, new Conjunction(THO_order,THO_user)) );
	
	FOLAtom THO_processed = new FOLAtom( new Predicate("processed", 1) );
	THO_processed.addArgument(doc);
	Condition THO_fs = new Condition( new Conjunction(THO_processed, THO_order));
	
	Goal THO = new Goal("to_handle_order", THO_tc, THO_fs);
	
	/*to_wait_order*/
	FOLAtom TWO_received = new FOLAtom( new Predicate("received",2));
	TWO_received.addArgument(doc);
	TWO_received.addArgument(usr);
	FOLAtom TWO_order = new FOLAtom( new Predicate("order",1));
	TWO_order.addArgument(doc);
	FOLAtom TWO_user = new FOLAtom( new Predicate("user",1));
	TWO_user.addArgument(usr);
	Condition TWO_tc = new Condition( new Conjunction(TWO_received, new Conjunction(TWO_order,TWO_user)) );
	
	FOLAtom TWO_available = new FOLAtom( new Predicate("available",1));
	TWO_available.addArgument(doc);
	Condition TWO_fs = new Condition( new Conjunction(TWO_available, TWO_order) );
	
	Goal TWO = new Goal("to_wait_order", TWO_tc, TWO_fs);
	
	/*to_process_order*/
	FOLAtom TPO_available = new FOLAtom( new Predicate("available",1));
	TPO_available.addArgument(doc);
	FOLAtom TPO_order = new FOLAtom( new Predicate("order",1));
	TPO_order.addArgument(doc);
	FOLAtom TPO_registered = new FOLAtom( new Predicate("registered",1));
	TPO_registered.addArgument(usr);
	FOLAtom TPO_user = new FOLAtom( new Predicate("user",1));
	TPO_user.addArgument(usr);
	Condition TPO_tc = new Condition( new Conjunction(new Conjunction(TPO_available,TPO_order), new Conjunction(TPO_registered,TPO_user)) );
	
	FOLAtom TPO_processed = new FOLAtom( new Predicate("processed", 1) );
	TPO_processed.addArgument(doc);
	Condition TPO_fs = new Condition( new Conjunction(TPO_processed, TPO_order));
	
	Goal TPO = new Goal("to_handle_order", TPO_tc, TPO_fs);
	
	/*to_process_accepted_order*/
	FOLAtom TPAO_accepted = new FOLAtom( new Predicate("accepted",1));
	TPAO_accepted.addArgument(doc);
	FOLAtom TPAO_order = new FOLAtom( new Predicate("order",1));
	TPAO_order.addArgument(doc);
	Condition TPAO_tc = new Condition( new Conjunction(TPAO_accepted, TPAO_order));
	
	FOLAtom TPAO_send = new FOLAtom( new Predicate("send",2));
	TPAO_send.addArgument(doc);
	TPAO_send.addArgument(mng);
	FOLAtom TPAO_delivery = new FOLAtom( new Predicate("delivery_order", 1));
	TPAO_delivery.addArgument(doc);
	FOLAtom TPAO_manager = new FOLAtom(new Predicate("storehouse_manager", 1));
	TPAO_manager.addArgument(mng);
	Condition TPAO_fs = new Condition( new Conjunction(TPAO_send, new Conjunction(TPAO_delivery, TPAO_manager)));
	
	Goal TPAO = new Goal("to_handle_order", TPAO_tc, TPAO_fs);
	
	/*to_notify_invoce*/
	FOLAtom TNI_registered = new FOLAtom( new Predicate("registered",1));
	TNI_registered.addArgument(usr);
	FOLAtom TNI_user = new FOLAtom( new Predicate("user",1));
	TNI_user.addArgument(usr);
	FOLAtom TNI_available = new FOLAtom( new Predicate("available",1));
	TNI_available.addArgument(doc);
	FOLAtom TNI_invoice = new FOLAtom( new Predicate("invoice",1));
	TNI_invoice.addArgument(doc);
	Condition TNI_tc = new Condition( new Conjunction(new Conjunction(TNI_registered, TNI_user), new Conjunction(TNI_available, TNI_invoice)) );
	
	FOLAtom TNI_send = new FOLAtom( new Predicate("send",2));
	TNI_send.addArgument(doc);
	TNI_send.addArgument(usr);		
	Condition TNI_fs = new Condition( new Conjunction(TNI_send, new Conjunction(TNI_invoice, TNI_user)) );
	
	Goal TNI = new Goal("to_notify_invoice", TNI_tc, TNI_fs);
	
	/*to_deliver_order*/
	FOLAtom TDO_send = new FOLAtom( new Predicate("send",2));
	TDO_send.addArgument(doc);
	TDO_send.addArgument(usr);
	FOLAtom TDO_invoice = new FOLAtom( new Predicate("invoice",1));
	TDO_invoice.addArgument(doc);		
	FOLAtom TDO_user = new FOLAtom( new Predicate("user",1));
	TDO_user.addArgument(usr);
	Condition TDO_tc = new Condition( new Conjunction(TDO_send, new Conjunction(TDO_invoice, TDO_user)) );
	
	FOLAtom TDO_delivery = new FOLAtom( new Predicate("delivery_order", 1));
	TDO_delivery.addArgument(doc);
	FOLAtom TDO_manager = new FOLAtom(new Predicate("storehouse_manager", 1));
	TDO_manager.addArgument(mng);
	Condition TDO_fs = new Condition( new Conjunction(TDO_send, new Conjunction(TDO_delivery, TDO_manager)) );
	
	Goal TDO = new Goal("to_delivery_order", TDO_tc, TDO_fs);
	
	/*to_notify_failure*/
	FOLAtom TNF_refused = new FOLAtom( new Predicate("refused",1));
	TNF_refused.addArgument(doc);
	FOLAtom TNF_order = new FOLAtom( new Predicate("order",1));
	TNF_order.addArgument(doc);
	FOLAtom TNF_registered = new FOLAtom( new Predicate("registered",1));
	TNF_registered.addArgument(usr);
	FOLAtom TNF_user = new FOLAtom( new Predicate("user",1));
	TNF_user.addArgument(usr);
	Condition TNF_tc = new Condition( new Conjunction(new Conjunction(TNF_refused, TNF_order), new Conjunction(TNF_registered, TNF_user)) );
	
	FOLAtom TNF_send = new FOLAtom( new Predicate("send",2));
	TNF_send.addArgument(new Constant("failure_order"));
	TNF_send.addArgument(usr);	
	Condition TNF_fs = new Condition( new Conjunction(TNF_send, TNF_user));
	
	Goal TNF = new Goal("to_notify_failure", TNF_tc, TNF_fs);
	
	/*******************/
	
	Constant a_user = new Constant("a_user");
	Constant an_order = new Constant("an_order");
	Constant the_user_data = new Constant("the_user_data");
	Constant the_registration_form = new Constant("the_registration_form");
	Constant failure_order = new Constant("failure_order");
	Constant the_invoice = new Constant("the_invoice");
	Constant the_delivery_order = new Constant("the_delivery_order");
	Constant a_storehouse_manager = new Constant("a_storehouse_manager");
	
	/*check_user*/
	FOLAtom CU_available = new FOLAtom( new Predicate("available",1));
	CU_available.addArgument(doc);
	FOLAtom CU_order = new FOLAtom( new Predicate("order",1));
	CU_order.addArgument(doc);
	Condition CU_pre = new Condition( new Conjunction(CU_available, CU_order) );

	Set<EvolutionScenario> CU_evo = new HashSet<>();
	CapabilityEvolutionScenario CU_evo1 = new CapabilityEvolutionScenario("RegisteredUserWithCloud");
	CU_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("registered", a_user)) ) );
	CU_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("has_cloud_space", a_user)) ) );
	CU_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	CU_evo.add(CU_evo1);
	CapabilityEvolutionScenario CU_evo2 = new CapabilityEvolutionScenario("RegisteredUserWithoutCloud");
	CU_evo2.addOperator( new AddStatement( new DLPHead(new DLPAtom("registered", a_user)) ) );
	CU_evo2.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	CU_evo.add(CU_evo2);
	CapabilityEvolutionScenario CU_evo3 = new CapabilityEvolutionScenario("KnownUser");
	CU_evo3.addOperator( new AddStatement( new DLPHead(new DLPAtom("complete", the_user_data)) ) );
	CU_evo3.addOperator( new AddStatement( new DLPHead(new DLPAtom("user_data", the_user_data)) ) );
	CU_evo3.addOperator( new AddStatement( new DLPHead(new DLPAtom("unregistered", a_user)) ) );
	CU_evo3.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	CU_evo.add(CU_evo3);
	CapabilityEvolutionScenario CU_evo4 = new CapabilityEvolutionScenario("UnknownUser");
	CU_evo4.addOperator( new AddStatement( new DLPHead(new DLPAtom("uncomplete", the_user_data)) ) );
	CU_evo4.addOperator( new AddStatement( new DLPHead(new DLPAtom("user_data", the_user_data)) ) );
	CU_evo4.addOperator( new AddStatement( new DLPHead(new DLPAtom("unregistered", a_user)) ) );
	CU_evo4.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	CU_evo.add(CU_evo4);
	
	AbstractCapability CU = new AbstractCapability("check_user", CU_evo, CU_pre, null);
	
	/*add_user*/
	FOLAtom AU_complete = new FOLAtom( new Predicate("complete",1));
	AU_complete.addArgument(doc);
	FOLAtom AU_user_data = new FOLAtom( new Predicate("user_data",1));
	AU_user_data.addArgument(doc);
	FOLAtom AU_unregistered = new FOLAtom( new Predicate("unregistered",1));
	AU_unregistered.addArgument(usr);
	FOLAtom AU_user = new FOLAtom( new Predicate("user",1));
	AU_user.addArgument(usr);
	Set AU_Set = new HashSet<Variable>();
	AU_Set.add(doc);
	AU_Set.add(usr);
	Condition AU_pre = new Condition(new ExistsQuantifiedFormula(new Conjunction(new Conjunction(AU_complete, AU_user_data), new Conjunction(AU_unregistered, AU_user)), AU_Set));

	Set<EvolutionScenario> AU_evo = new HashSet<>();
	CapabilityEvolutionScenario AU_evo1 = new CapabilityEvolutionScenario("RegisteredUser");
	AU_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("registered", a_user)) ) );
	AU_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	AU_evo.add(AU_evo1);
	
	AbstractCapability AU = new AbstractCapability("add_user", AU_evo, AU_pre, null);
	
	/*send_registration_form*/
	FOLAtom SRF_uncomplete = new FOLAtom( new Predicate("uncomplete",1));
	SRF_uncomplete.addArgument(doc);
	FOLAtom SRF_user_data = new FOLAtom( new Predicate("user_data",1));
	SRF_user_data.addArgument(doc);
	FOLAtom SRF_unregistered = new FOLAtom( new Predicate("unregistered",1));
	SRF_unregistered.addArgument(usr);
	FOLAtom SRF_user = new FOLAtom( new Predicate("user",1));
	SRF_user.addArgument(usr);
	Set SRF_Set = new HashSet<Variable>();
	SRF_Set.add(doc);
	SRF_Set.add(usr);
	Condition SRF_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(SRF_uncomplete, SRF_user_data), new Conjunction(SRF_unregistered, SRF_user)), SRF_Set ));

	Set<EvolutionScenario> SRF_evo = new HashSet<>();
	CapabilityEvolutionScenario SRF_evo1 = new CapabilityEvolutionScenario("UncompleteRegistrationForm");
	SRF_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("uncomplete", the_registration_form)) ) );
	SRF_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("registration_form", the_registration_form)) ) );
	SRF_evo.add(SRF_evo1);
	
	AbstractCapability SRF = new AbstractCapability("send_registration_form", SRF_evo, SRF_pre, null);
	
	/*wait_user_data*/
	FOLAtom WUD_uncomplete = new FOLAtom( new Predicate("uncomplete",1));
	WUD_uncomplete.addArgument(doc);
	FOLAtom WUD_registration_form = new FOLAtom( new Predicate("registration_form",1));
	WUD_registration_form.addArgument(doc);
	Condition WUD_pre = new Condition( new ExistsQuantifiedFormula( new Conjunction(WUD_uncomplete, WUD_registration_form), doc));

	Set<EvolutionScenario> WUD_evo = new HashSet<>();
	CapabilityEvolutionScenario WUD_evo1 = new CapabilityEvolutionScenario("CompleteForm");
	WUD_evo1.addOperator( new RemoveStatement( new DLPHead(new DLPAtom("uncomplete", the_registration_form)) ) );
	WUD_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("complete", the_user_data)) ) );
	WUD_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("user_data", the_user_data)) ) );
	WUD_evo.add(WUD_evo1);
	CapabilityEvolutionScenario WUD_evo2 = new CapabilityEvolutionScenario("UncompleteForm");
	WUD_evo2.addOperator( new RemoveStatement( new DLPHead(new DLPAtom("uncomplete", the_registration_form)) ) );
	WUD_evo.add(WUD_evo2);
	
	AbstractCapability WUD = new AbstractCapability("wait_user_data", WUD_evo, WUD_pre, null);
	
	/*check_storehouse*/
	FOLAtom CS_available = new FOLAtom( new Predicate("available",1));
	CS_available.addArgument(doc);
	FOLAtom CS_order = new FOLAtom( new Predicate("order",1));
	CS_order.addArgument(doc);
	FOLAtom CS_registered = new FOLAtom( new Predicate("registered",1));
	CS_registered.addArgument(usr);
	FOLAtom CS_user = new FOLAtom( new Predicate("user",1));
	CS_user.addArgument(usr);
	Set CS_Set = new HashSet<Variable>();
	CS_Set.add(doc);
	CS_Set.add(usr);
	Condition CS_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(CS_available, CS_order), new Conjunction(CS_registered, CS_user)), CS_Set ));

	Set<EvolutionScenario> CS_evo = new HashSet<>();
	CapabilityEvolutionScenario CS_evo1 = new CapabilityEvolutionScenario("AcceptableOrder");
	CS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("accepted", an_order)) ) );
	CS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("order", an_order)) ) );
	CS_evo.add(CS_evo1);
	CapabilityEvolutionScenario CS_evo2 = new CapabilityEvolutionScenario("UnacceptableOrder");
	CS_evo2.addOperator( new AddStatement( new DLPHead(new DLPAtom("refused", an_order)) ) );
	CS_evo2.addOperator( new AddStatement( new DLPHead(new DLPAtom("order", an_order)) ) );
	CS_evo.add(CS_evo2);
	
	AbstractCapability CS = new AbstractCapability("check_storehouse", CS_evo, CS_pre, null);
	
	/*notify_stock_failure*/
	FOLAtom NSF_refused = new FOLAtom( new Predicate("refused",1));
	NSF_refused.addArgument(doc);
	FOLAtom NSF_order = new FOLAtom( new Predicate("order",1));
	NSF_order.addArgument(doc);
	FOLAtom NSF_registered = new FOLAtom( new Predicate("registered",1));
	NSF_registered.addArgument(usr);
	FOLAtom NSF_user = new FOLAtom( new Predicate("user",1));
	NSF_user.addArgument(usr);
	Set NSF_Set = new HashSet<Variable>();
	NSF_Set.add(doc);
	NSF_Set.add(usr);
	Condition NSF_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(NSF_refused, NSF_order), new Conjunction(NSF_registered, NSF_user)), NSF_Set ));

	Set<EvolutionScenario> NSF_evo = new HashSet<>();
	CapabilityEvolutionScenario NSF_evo1 = new CapabilityEvolutionScenario("Failure");
	NSF_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("sent", failure_order, a_user)) ) );
	NSF_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	NSF_evo.add(NSF_evo1);
	
	AbstractCapability NSF = new AbstractCapability("notify_stock_failure", NSF_evo, NSF_pre, null);
	
	/*generate_invoice*/
	FOLAtom GI_accepted = new FOLAtom( new Predicate("accepted",1));
	GI_accepted.addArgument(doc);
	FOLAtom GI_order = new FOLAtom( new Predicate("order",1));
	GI_order.addArgument(doc);
	FOLAtom GI_registered = new FOLAtom( new Predicate("registered",1));
	GI_registered.addArgument(usr);
	FOLAtom GI_user = new FOLAtom( new Predicate("user",1));
	GI_user.addArgument(usr);
	Set GT_Set = new HashSet<Variable>();
	GT_Set.add(doc);
	GT_Set.add(usr);
	Condition GI_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(GI_accepted, GI_order), new Conjunction(GI_registered, GI_user)), GT_Set) );

	Set<EvolutionScenario> GI_evo = new HashSet<>();
	CapabilityEvolutionScenario GI_evo1 = new CapabilityEvolutionScenario("AvailableInvoice");
	GI_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("available", the_invoice)) ) );
	GI_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("invoice", the_invoice)) ) );
	GI_evo.add(GI_evo1);
	
	AbstractCapability GI = new AbstractCapability("generate_invoice", GI_evo, GI_pre, null);
	
	/*upload_on_user_cloud_storage*/
	FOLAtom UOUCS_available = new FOLAtom( new Predicate("available",1));
	UOUCS_available.addArgument(doc);
	FOLAtom UOUCS_invoice = new FOLAtom( new Predicate("invoice",1));
	UOUCS_invoice.addArgument(doc);
	FOLAtom UOUCS_has_cloud_space = new FOLAtom( new Predicate("has_cloud_space",1));
	UOUCS_has_cloud_space.addArgument(usr);
	FOLAtom UOUCS_user = new FOLAtom( new Predicate("user",1));
	UOUCS_user.addArgument(usr);
	Set UOUCS_Set = new HashSet<Variable>();
	UOUCS_Set.add(doc);
	UOUCS_Set.add(usr);
	Condition UOUCS_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(UOUCS_available, UOUCS_invoice), new Conjunction(UOUCS_has_cloud_space, UOUCS_user)), UOUCS_Set ));

	Set<EvolutionScenario> UOUCS_evo = new HashSet<>();
	CapabilityEvolutionScenario UOUCS_evo1 = new CapabilityEvolutionScenario("UploadedOnCloud");
	UOUCS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("uploaded_on_cloud", the_invoice)) ) );
	UOUCS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("invoice", the_invoice)) ) );
	UOUCS_evo.add(UOUCS_evo1);
	
	AbstractCapability UOUCS = new AbstractCapability("upload_on_user_cloud_storage", UOUCS_evo, UOUCS_pre, null);
	
	/*upload_on_private_cloud_storage*/
	FOLAtom UOPCS_available = new FOLAtom( new Predicate("available",1));
	UOPCS_available.addArgument(doc);
	FOLAtom UOPCS_invoice = new FOLAtom( new Predicate("invoice",1));
	UOPCS_invoice.addArgument(doc);
	FOLAtom UOPCS_not_has_cloud_space = new FOLAtom( new Predicate("has_cloud_space",1));
	UOPCS_not_has_cloud_space.addArgument(usr);
	Negation neg1 = new Negation(UOPCS_not_has_cloud_space);
	FOLAtom UOPCS_user = new FOLAtom( new Predicate("user",1));
	UOPCS_user.addArgument(usr);
	Set UOPCS_Set = new HashSet<Variable>();
	UOPCS_Set.add(doc);
	UOPCS_Set.add(usr);
	Condition UOPCS_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(UOPCS_available, UOPCS_invoice), new Conjunction(neg1, UOPCS_user)), UOPCS_Set ));

	Set<EvolutionScenario> UOPCS_evo = new HashSet<>();
	CapabilityEvolutionScenario UOPCS_evo1 = new CapabilityEvolutionScenario("UploadedOnCloud");
	UOPCS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("uploaded_on_cloud", the_invoice)) ) );
	UOPCS_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("invoice", the_invoice)) ) );
	UOPCS_evo.add(UOPCS_evo1);
	
	AbstractCapability UOPCS = new AbstractCapability("upload_on_private_cloud_storage", UOPCS_evo, UOPCS_pre, null);
	
	/*share_file_link*/
	FOLAtom SFL_uploaded_on_cloud = new FOLAtom( new Predicate("uploaded_on_cloud",1));
	SFL_uploaded_on_cloud.addArgument(doc);
	FOLAtom SFL_invoice = new FOLAtom( new Predicate("invoice",1));
	SFL_invoice.addArgument(doc);
	FOLAtom SFL_not_has_cloud_space = new FOLAtom( new Predicate("has_cloud_space",1));
	SFL_not_has_cloud_space.addArgument(usr);
	Negation neg2 = new Negation(SFL_not_has_cloud_space);
	FOLAtom SFL_user = new FOLAtom( new Predicate("user",1));
	SFL_user.addArgument(usr);
	Set SFL_Set = new HashSet<Variable>();
	SFL_Set.add(doc);
	SFL_Set.add(usr);
	Condition SFL_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(new Conjunction(SFL_uploaded_on_cloud, SFL_invoice), new Conjunction(neg2, SFL_user)), SFL_Set ));

	System.out.println("ciao" + SFL_pre.toString());
	
	Set<EvolutionScenario> SFL_evo = new HashSet<>();
	CapabilityEvolutionScenario SFL_evo1 = new CapabilityEvolutionScenario("MailedPermLink");
	SFL_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("mailed_perm_link", the_invoice, a_user)) ) );
	SFL_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("invoice", the_invoice)) ) );
	SFL_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("user", a_user)) ) );
	SFL_evo.add(SFL_evo1);
	
	AbstractCapability SFL = new AbstractCapability("share_file_link", SFL_evo, SFL_pre, null);
	
	/*notify_storehouse_manager*/
	FOLAtom NSM_notified = new FOLAtom( new Predicate("notified",2));
	NSM_notified.addArgument(doc);
	NSM_notified.addArgument(usr);
	FOLAtom NSM_invoice = new FOLAtom( new Predicate("invoice",1));
	NSM_invoice.addArgument(doc);
	FOLAtom NSM_user = new FOLAtom( new Predicate("user",1));
	NSM_user.addArgument(usr);
	Set NSM_Set = new HashSet<Variable>();
	NSM_Set.add(doc);
	NSM_Set.add(usr);
	Condition NSM_pre = new Condition(new ExistsQuantifiedFormula( new Conjunction(NSM_notified, new Conjunction(NSM_invoice, NSM_user)), NSM_Set ));

	Set<EvolutionScenario> NSM_evo = new HashSet<>();
	CapabilityEvolutionScenario NSM_evo1 = new CapabilityEvolutionScenario("SentDeliveryOrderToStorehouseManager");
	NSM_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("sent", the_delivery_order , a_storehouse_manager)) ) );
	NSM_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("delivery_order", the_delivery_order)) ) );
	NSM_evo1.addOperator( new AddStatement( new DLPHead(new DLPAtom("storehouse_manager", a_storehouse_manager)) ) );
	NSM_evo.add(NSM_evo1);
	
	AbstractCapability NSM = new AbstractCapability("notify_storehouse_manager", NSM_evo, NSM_pre, null);
	
	this.wStart = new StateOfWorld();
	
	try {
		this.wStart.addFact_asString("order(an_order).");
		this.wStart.addFact_asString("available(an_order).");
		this.wStart.addFact_asString("user(a_user).");
		this.wStart.addFact_asString("user_data(the_user_data).");
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
		e.printStackTrace();
	}
	
	this.regAndCloud = new StateOfWorld();
	try {
		this.regAndCloud.addFact_asString("order(an_order).");
		this.regAndCloud.addFact_asString("available(an_order).");
		this.regAndCloud.addFact_asString("user(a_user).");
		this.regAndCloud.addFact_asString("user_data(the_user_data).");
		this.regAndCloud.addFact_asString("registered(a_user).");
		this.regAndCloud.addFact_asString("has_cloud_space(a_user).");
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
		e.printStackTrace();
	}
	
	this.regNoCloud = new StateOfWorld();
	try {
		this.regNoCloud.addFact_asString("order(an_order).");
		this.regNoCloud.addFact_asString("available(an_order).");
		this.regNoCloud.addFact_asString("user(a_user).");
		this.regNoCloud.addFact_asString("user_data(the_user_data).");
		this.regNoCloud.addFact_asString("registered(a_user).");
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
		e.printStackTrace();
	}
	
	this.unknown = new StateOfWorld();
	try {
		this.unknown.addFact_asString("order(an_order).");
		this.unknown.addFact_asString("available(an_order).");
		this.unknown.addFact_asString("user(a_user).");
		this.unknown.addFact_asString("user_data(the_user_data).");
		this.unknown.addFact_asString("uncomplete(the_user_data).");
		this.unknown.addFact_asString("unregistered(a_user).");
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
		e.printStackTrace();
	}
	
	this.known = new StateOfWorld();
	try {
		this.known.addFact_asString("order(an_order).");
		this.known.addFact_asString("available(an_order).");
		this.known.addFact_asString("user(a_user).");
		this.known.addFact_asString("user_data(the_user_data).");
		this.known.addFact_asString("complete(the_user_data).");
		this.known.addFact_asString("unregistered(a_user).");
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (layer.semantic.exception.NotAllowedInAStateOfWorld e) {
		e.printStackTrace();
	}

	this.noderegAndCloud = new WorldNode(this.regAndCloud);
	this.noderegNoCloud = new WorldNode(this.regNoCloud);
	this.nodeknown = new WorldNode(this.known);
	this.nodeunknown = new WorldNode(this.unknown);



	/*
	this.wStart.addFact_asASP(new DLPHead(new DLPAtom("order", an_order)));
	this.wStart.addFact_asASP(new DLPHead(new DLPAtom()));
	this.wStart.addFact_asASP(new DLPHead(new DLPAtom("available", an_order)));
	this.wStart.addFact_asASP(new DLPHead(new DLPAtom("user", a_user)));
	this.wStart.addFact_asASP(new DLPHead(new DLPAtom("user_data", the_user_data)));
	*/
	this.nodewStart = new WorldNode(this.wStart);
	
	/*Model construction*/
    model = new GoalModel(THO);
    ArrayList<Goal> firstLevel = new ArrayList<>();
    firstLevel.add(TWO);
    firstLevel.add(TPO);
    ArrayList<Goal> secondLevel = new ArrayList<>();
    secondLevel.add(TPAO);
    secondLevel.add(TNF);
    ArrayList<Goal> thirdLevel = new ArrayList<>();
    thirdLevel.add(TNI);
    thirdLevel.add(TDO);
    
    model.addAndArcs(THO, firstLevel);
    model.addOrArcs(TPO, secondLevel);
    model.addAndArcs(TPAO, thirdLevel);
    
    this.capList = new ArrayList<>();
    capList.add(this.CU);
    this.exploration = new ProblemExploration(model, capList, domain);
    
	this.startTokens = new ArrayList<>();
	this.startTokens.add(new Token("p3"));
	this.startTokens.add(new Token("p4"));
	
    this.eStart = new ENode(nodewStart);
    this.eStart.setTokens(startTokens);
}
	

	@Test
	public void expandNodeTest(){
		this.exploration.addToVisit(this.nodewStart, this.startTokens, 9);
		this.exploration.expandNode();
		assertEquals(true,  this.exploration.);
	}
}


