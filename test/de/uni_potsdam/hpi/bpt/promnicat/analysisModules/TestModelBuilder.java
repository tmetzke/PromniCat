/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules;

import org.jbpt.pm.Activity;
import org.jbpt.pm.AlternativGateway;
import org.jbpt.pm.AndGateway;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.Event;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.pm.bpmn.EndEvent;
import org.jbpt.pm.bpmn.StartEvent;


/**
 * This class provides some models, that can be used for testing.
 * @author Tobias Hoppe
 *
 */
public class TestModelBuilder {

	/**
	 * @return the following connected {@link ProcessModel}:
	 *
	 * 						 |->e5
	 * 						 |
	 * 			|->t2->e4->and3------------------->alt1->e2
	 * 			|									|
	 * e1->t1->or1									|
	 * 			|		|->e3--------------->and2-->|	
	 * 			|		|					  |
	 * 			|->t3->and1					  |
	 * 					|		|->t5-->|	  |
	 * 					|---->xor1		xor3->|
	 * 							|->t4-->|
	 * 
	 */
	public static ProcessModel getConnectedProcessModel() {
		ProcessModel model = new ProcessModel();
		
		StartEvent e1 = new StartEvent("e1");
		Event e3 = new Event("e3");
		Event e4 = new Event("e4");
		EndEvent e2 = new EndEvent("e2");
		EndEvent e5 = new EndEvent("e5");
		Activity t1 = new Activity("t1");
		Activity t2 = new Activity("t2");
		Activity t3 = new Activity("t3");
		Activity t4 = new Activity("t4");
		Activity t5 = new Activity("t5");
		OrGateway or1 = new OrGateway("or1");
		XorGateway xor1 = new XorGateway("xor1");
		AlternativGateway alt1 = new AlternativGateway("alt1");
		XorGateway xor3 = new XorGateway("xor3");
		AndGateway and1 = new AndGateway("and1");
		AndGateway and2 = new AndGateway("and2");
		AndGateway and3 = new AndGateway("and3");
		
		model.addControlFlow(e1, t1);
		model.addControlFlow(t1, or1);
		model.addControlFlow(or1, t2);
		model.addControlFlow(t2, e4);
		model.addControlFlow(e4, and3);
		model.addControlFlow(and3, e5);
		model.addControlFlow(and3, alt1);
		model.addControlFlow(alt1, e2);
		model.addControlFlow(or1, t3);
		model.addControlFlow(t3, and1);
		model.addControlFlow(and1, e3);
		model.addControlFlow(e3, and2);
		model.addControlFlow(and2, alt1);
		model.addControlFlow(and1, xor1);
		model.addControlFlow(xor1, t5);
		model.addControlFlow(xor1, t4);
		model.addControlFlow(t5, xor3);
		model.addControlFlow(t4, xor3);
		model.addControlFlow(xor3, and2);
		
		return model;
	}
	
	/**
	 * @return a {@link ProcessModel} without edges,
	 * 5 {@link Activity}s, 3 {@link Gateway}s, 4 {@link Event}s, 6 {@link DataNode}s
	 */
	public static ProcessModel getNodesOnlyProcessModel() {
		ProcessModel model = new ProcessModel();
		for (int i = 0; i < 5; i++) {
			model.addFlowNode(new Activity("t" + i));
		}
		for (int i = 0; i < 3; i++) {
			model.addFlowNode(new AlternativGateway("g" + i));
		}
		for (int i = 0; i < 4; i++) {
			model.addFlowNode(new Event("e" + i));
		}
		for (int i = 0; i < 6; i++) {
			model.addNonFlowNode(new DataNode("d" + i));
		}
		return model;
	}
	
	/**
	 * @return the following {@link ProcessModel} containing loops:
	 *
	 *	|<-----------------------------|
	 * 	|	|<-----|	|<-t4<---|	   |
	 * 	|	|	   |	|		 |	   |
	 * e1->or1--->and1->t1->t2->xor1->e2
	 * 				|			 |
	 * 				|-->t3------>|
	 * 
	 */
	public static ProcessModel getModelWithLoops() {
		ProcessModel model = new ProcessModel();
		
		StartEvent e1 = new StartEvent("e1");
		EndEvent e2 = new EndEvent("e2");
		Activity t1 = new Activity("t1");
		Activity t2 = new Activity("t2");
		Activity t3 = new Activity("t3");
		Activity t4 = new Activity("t4");
		OrGateway or1 = new OrGateway("or1");
		XorGateway xor1 = new XorGateway("xor1");
		AndGateway and1 = new AndGateway("and1");
		
		model.addControlFlow(e1, or1);
		model.addControlFlow(or1, and1);
		model.addControlFlow(and1, or1);
		model.addControlFlow(and1, t1);
		model.addControlFlow(t1, t2);
		model.addControlFlow(t2, xor1);
		model.addControlFlow(xor1, t4);
		model.addControlFlow(t4, t1);
		model.addControlFlow(and1, t3);
		model.addControlFlow(t3, xor1);
		model.addControlFlow(xor1, e2);
		model.addControlFlow(e2, e1);
		
		return model;
	}
	
	/**
	 * @return the following disconnected {@link ProcessModel}:
	 * 
	 * e1->t1->t2		t3		t4->t5----->t6		t8
	 * 								|		|
	 * 								|->t7-->|
	 * 
	 */
	public static ProcessModel getDisconnectedModel() {
		ProcessModel model = new ProcessModel();
		
		StartEvent e1 = new StartEvent("e1");
		Activity t1 = new Activity("t1");
		Activity t2 = new Activity("t2");
		Activity t3 = new Activity("t3");
		model.addFlowNode(t3);
		Activity t4 = new Activity("t4");
		Activity t5 = new Activity("t5");
		Activity t6 = new Activity("t6");
		Activity t7 = new Activity("t7");
		Activity t8 = new Activity("t8");
		model.addFlowNode(t8);
		
		model.addControlFlow(e1, t1);
		model.addControlFlow(t1, t2);
		
		model.addControlFlow(t4, t5);
		model.addControlFlow(t5, t6);
		model.addControlFlow(t5, t7);
		model.addControlFlow(t7, t6);
		
		return model;
		
	}
}
