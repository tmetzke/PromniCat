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
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.classification;

import java.util.ArrayList;
import java.util.Collection;

import org.jbpt.hypergraph.abs.GObject;
import org.jbpt.petri.PetriNet;
import org.jbpt.pm.Activity;
import org.jbpt.pm.AndGateway;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.Event;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.utils.TransformationException;

/**
 * @author Tobias Hoppe
 *
 */
public class ProcessModelToPetriNetConverter {
	
	private static final String THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY = "The given ProcessModel contains at least one OrGateway.";
	private ProcessModel model = null;
	private PetriNet petriNet = null;

	/**
	 * Transforms the given {@link ProcessModel} into a {@link PetriNet}.
	 * <b><br/>Assumptions:</b><br/>
	 * - Model does not contain any {@link OrGateway}s
	 * 
	 * @param model to transform
	 * @return the created {@link PetriNet}
	 * @throws TransformationException if assumptions are violated.
	 */
	public PetriNet convertToPetriNet(ProcessModel model) throws TransformationException {
		if(model == null) {
			return null;
		}
		if(model.filter(OrGateway.class).size() > 0) {
			throw new TransformationException(THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY);
		}
		//clone given model to keep model changes within this instance
		this.model = model.clone();
		this.petriNet = new PetriNet();
		//copy id, name, desc, and tag
		copyAttributes(this.model, this.petriNet);
		
		//remove multiple incoming or outgoing edges by transforming model
		preProcessProcessModel();

		//create places and transitions according to the flow nodes of the model
		convertFlowNodes();
		//add edges according to the control flow of the model
		convertControlFlowEdges();
		return this.petriNet;
	}
	
	/**
	 * Connect the already parsed {@link FlowNode}s according to the {@link ControlFlow}
	 * of the {@link ProcessModel} to convert. 
	 * The {@link ProcessModel} shall include {@link Gateway}s,
	 * that are either split or join and {@link Activity}s and {@link Event}s
	 * shall have only one incoming and one outgoing {@link ControlFlow} edge.
	 */
	private void convertControlFlowEdges() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Transform each {@link FlowNode} into it's corresponding {@link PetriNet}
	 * representation. The {@link ProcessModel} shall include {@link Gateway}s,
	 * that are either split or join and {@link Activity}s and {@link Event}s
	 * shall have only one incoming and one outgoing {@link ControlFlow} edge.
	 * @throws TransformationException if an {@link OrGateway} should be converted
	 */
	private void convertFlowNodes() throws TransformationException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Remove multiple incoming or outgoing {@link ControlFlow} edges of an {@link Activity}
	 * or an {@link Event} by inserting {@link Gateway}s.
	 * Each {@link Gateway} having multiple incoming and outgoing edges is transformed into
	 * two {@link Gateway}s; one for the merge and the other one for the split.
	 * @throws TransformationException if an {@link OrGateway} should be converted
	 */
	private void preProcessProcessModel() throws TransformationException {
		for (FlowNode node : this.model.getFlowNodes()) {
			Collection<ControlFlow<FlowNode>> incomingControlFlows = this.model.getIncomingControlFlow(node);
			//insert xor gateway for activities and events with more than one incoming edge
			if(incomingControlFlows.size() > 1 && (node instanceof Activity || node instanceof Event)) {
				XorGateway xor = new XorGateway("pre_" + node.getName());
				for(ControlFlow<FlowNode> edge : incomingControlFlows) {
					edge.setTarget(xor);
				}
				this.model.addControlFlow(xor, node);
			}
			Collection<ControlFlow<FlowNode>> outgoingControlFlows = this.model.getOutgoingControlFlow(node);
			//insert and gateway / or gateway for activities and events with more than one outgoing edge
			if(outgoingControlFlows.size() > 1 && (node instanceof Activity || node instanceof Event)) {
				String name = "post_" + node.getName();
				Gateway g = new AndGateway(name);
				if(this.model instanceof Bpmn<?,?>) {
					Collection<Boolean> conditional = new ArrayList<Boolean>();
					for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
						if(!((BpmnControlFlow<FlowNode>)edge).hasCondition()) {
							conditional.add(false);
						}
					}
					if(conditional.size() < 2) {
						throw new TransformationException(THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY);
						//uncomment if or gateways could be handled
						//g = new OrGateway(name);
					}
				}
				for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
					edge.setSource(g);
				}
				this.model.addControlFlow(node, g);		
			}
			//create two gateways for a gateway being split and join
			if(node instanceof Gateway && outgoingControlFlows.size() > 1 && incomingControlFlows.size() > 1) {
				Gateway g = (Gateway) node.clone();
				for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
					edge.setSource(g);
				}
				this.model.addControlFlow(node, g);
			}			
		}
	}

	/**
	 * Copies the id, the name, the description, and the tag attribute from one
	 * element to the other.
	 * @param from element to copy the attributes from
	 * @param to element to set the attributes of
	 */
	private void copyAttributes(GObject from, GObject to) {
		to.setId(from.getId());
		to.setName(from.getName());
		to.setDescription(from.getDescription());
		to.setTag(from.getTag());
	}
}
