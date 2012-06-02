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
package de.uni_potsdam.hpi.bpt.promnicat.modelConverter;

import java.util.ArrayList;
import java.util.Collection;

import org.jbpt.petri.PetriNet;
import org.jbpt.pm.Activity;
import org.jbpt.pm.AndGateway;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.Event;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.utils.TransformationException;

/**
 * @author Tobias Hoppe
 *
 */
public class BpmnToPetriNetConverter extends AbstractModelToPetriNetConverter {

	/**
	 * Transforms the given {@link ProcessModel} into a {@link PetriNet}.
	 * <b><br/>Assumptions:</b><br/>
	 * - Model does not contain any {@link OrGateway}s
	 * 
	 * @param model to transform
	 * @return the created {@link PetriNet}
	 * @throws TransformationException if assumptions are violated.
	 */
	@Override
	public PetriNet convertToPetriNet(ProcessModel model) throws TransformationException {
		if(!(model instanceof Bpmn<?, ?>)) {
			throw new IllegalArgumentException(THE_GIVEN_PROCESS_MODEL_CAN_NOT_BE_HANDELED_BY_THIS_CONVERTER);
		}
		ProcessModel transformedModel = prepareProcessModel(model);
		if(transformedModel == null) {
			return null;
		}
		//create places and transitions according to the flow nodes of the model
		convertFlowNodes(transformedModel.getFlowNodes());
		//add edges according to the control flow of the model
		convertControlFlowEdges(transformedModel.getControlFlow());
		return this.petriNet;
	}
	
	/**
	 * Transform {@link Activity}s or {@link Event}s with multiple outgoing edges
	 * by inserting additional {@link Gateway}s.
	 * @param model to pre-process
	 * @param node to analyze
	 * @param outgoingControlFlows outgoing edges of current node
	 * @throws TransformationException if an {@link OrGateway} shall be created
	 */
	@Override
	protected void preProcessMultiOutgoingEdges(ProcessModel model, FlowNode node,
			Collection<ControlFlow<FlowNode>> outgoingControlFlows) throws TransformationException {
		String name = "post_" + node.getName();
		Gateway g = new AndGateway(name);
		if(model instanceof Bpmn<?,?>) {
			Collection<Boolean> conditional = new ArrayList<Boolean>();
			for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
				if(!((BpmnControlFlow<FlowNode>)edge).hasCondition()) {
					conditional.add(false);
				}
			}
			if(conditional.size() >= outgoingControlFlows.size() - 1) {
				throw new TransformationException(THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY);
				//uncomment if or gateways could be handled
				//g = new OrGateway(name);
			}
		}
		for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
			edge.setSource(g);
		}
		model.addControlFlow(node, g);
	}

}
