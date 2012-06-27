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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpt.hypergraph.abs.GObject;
import org.jbpt.petri.Flow;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.pm.Activity;
import org.jbpt.pm.AndGateway;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.Event;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.Gateway;
import org.jbpt.pm.OrGateway;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.XorGateway;
import org.jbpt.throwable.TransformationException;

/**
 * Abstract base class for {@link ProcessModel} to {@link PetriNet} converter.
 * Provides some fields needed in all sub-types and some basic transformations.
 * @author Tobias Hoppe
 *
 */
public abstract class AbstractModelToPetriNetConverter implements IModelToPetriNetConverter {

	protected static final String THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY = "The given ProcessModel contains at least one OrGateway.";
	protected static final String THE_GIVEN_PROCESS_MODEL_CAN_NOT_BE_HANDELED_BY_THIS_CONVERTER = "The given process model can not be handeled by this converter!";
	
	/**
	 * PetriNet converted from given model
	 */
	protected PetriNet petriNet = null;
	
	/**
	 * Mapping from model element to 
	 */
	protected Map<FlowNode, Node> nodeMapping = new HashMap<FlowNode, Node>();
	
	/**
	 * Identifier used for additional created model elements. Shall be
	 * extended by prefix or suffix to guaranty uniqueness.
	 */
	private long id = 0;

	protected long getNextId() {
		return this.id++;
	}
	
	@Override
	public PetriNet getLastPetriNet() {
		return this.petriNet;
	}
	
	/**
	 * Initializes {@link PetriNet} and pre-process the given {@link ProcessModel}.
	 * @param model to pre-process
	 * @return the pre-processed model or <code>null</code> if the given {@link ProcessModel}
	 * is <code>null</code>.
	 * @throws TransformationException if the created {@link ProcessModel} contains an {@link OrGateway}.
	 */
	protected ProcessModel prepareProcessModel(ProcessModel model) throws TransformationException {
		if(model == null) {
			return null;
		}
		//remove multiple incoming or outgoing edges by transforming model
		ProcessModel transformedModel = preProcessProcessModel(model);
		if(model.filter(OrGateway.class).size() > 0) {
			throw new TransformationException(THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY);
		}
		
		//initialize internal data structures
		this.petriNet = new PetriNet();
		this.nodeMapping.clear();
		//copy id, name, desc, and tag
		copyAttributes(model, this.petriNet);
		
		return transformedModel;
	}
	
	/**
	 * Remove multiple incoming or outgoing {@link ControlFlow} edges of an {@link Activity}
	 * or an {@link Event} by inserting {@link Gateway}s.
	 * Each {@link Gateway} having multiple incoming and outgoing edges is transformed into
	 * two {@link Gateway}s; one for the merge and the other one for the split.
	 * @param model to transform into {@link ProcessModel} ready for conversion to {@link PetriNet}.
	 * @return the transformed {@link ProcessModel} ready for conversion to {@link PetriNet}. 
	 * @throws TransformationException if an {@link OrGateway} shall be created
	 */
	protected ProcessModel preProcessProcessModel(ProcessModel model) throws TransformationException {
		ProcessModel result = model.clone();
		for (FlowNode node : model.getFlowNodes()) {
			Collection<ControlFlow<FlowNode>> incomingControlFlows = model.getIncomingControlFlow(node);
			//insert xor gateway for activities and events with more than one incoming edge
			if(incomingControlFlows.size() > 1 && (node instanceof Activity || node instanceof Event)) {
				XorGateway xor = new XorGateway("pre_" + node.getName());
				for(ControlFlow<FlowNode> edge : incomingControlFlows) {
					edge.setTarget(xor);
				}
				model.addControlFlow(xor, node);
			}
			Collection<ControlFlow<FlowNode>> outgoingControlFlows = model.getOutgoingControlFlow(node);
			//insert and gateway / or gateway for activities and events with more than one outgoing edge
			if(outgoingControlFlows.size() > 1 && (node instanceof Activity || node instanceof Event)) {
				preProcessMultiOutgoingEdges(model, node, outgoingControlFlows);		
			}
			//create two gateways for a gateway being split and join
			if(node instanceof Gateway && outgoingControlFlows.size() > 1 && incomingControlFlows.size() > 1) {
				Gateway g = (Gateway) node.clone();
				for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
					edge.setSource(g);
				}
				model.addControlFlow(node, g);
			}			
		}
		return result;
	}

	/**
	 * Transform {@link Activity}s or {@link Event}s with multiple outgoing edges
	 * by inserting additional {@link Gateway}s.
	 * @param model to pre-process
	 * @param node to analyze
	 * @param outgoingControlFlows outgoing edges of current node
	 */
	protected void preProcessMultiOutgoingEdges(ProcessModel model, FlowNode node,
			Collection<ControlFlow<FlowNode>> outgoingControlFlows) throws TransformationException {
		String name = "post_" + node.getName();
		Gateway g = new AndGateway(name);
		for(ControlFlow<FlowNode> edge : outgoingControlFlows) {
			edge.setSource(g);
		}
		model.addControlFlow(node, g);
	}
	
	/**
	 * Copies the id, the name, the description, and the tag attribute from one
	 * element to the other.
	 * @param from element to copy the attributes from
	 * @param to element to set the attributes of
	 */
	protected void copyAttributes(GObject from, GObject to) {
		to.setId(from.getId());
		to.setName(from.getName());
		to.setDescription(from.getDescription());
		to.setTag(from.getTag());
	}
	
	/**
	 * Converts the given {@link Gateway} into it's corresponding {@link PetriNet} 
	 * representation. <br/>
	 * {@link XorGateway}s are converted to {@link Place}s and
	 * {@link AndGateway}s are converted to {@link Transition}s.
	 * @param gateway {@link Gateway} to convert
	 */
	protected void convertGateway(Gateway gateway) {
		if(gateway instanceof AndGateway) {
			//add a silent transition for AND-Gateway
			Transition t = new Transition();
			copyAttributes(gateway, t);
			//make it a silent transition
			t.setName("");
			this.petriNet.addTransition(t);
			this.nodeMapping.put(gateway, t);
		} else if(gateway instanceof XorGateway) {
			//add a place for XOR-Gateway
			Place p = new Place();
			copyAttributes(gateway, p);
			this.petriNet.addPlace(p);
			this.nodeMapping.put(gateway, p);
		}		
	}
	
	/**
	 * Transform each {@link FlowNode} into it's corresponding {@link PetriNet}
	 * representation. The {@link ProcessModel} shall include {@link Gateway}s,
	 * that are either split or join and {@link Activity}s and {@link Event}s
	 * shall have only one incoming and one outgoing {@link ControlFlow} edge.
	 * @param flowNodes {@link FlowNode}s to convert
	 * @throws TransformationException if an {@link OrGateway} must be converted
	 */
	protected void convertFlowNodes(Collection<FlowNode> flowNodes) throws TransformationException {
		for(FlowNode flowNode : flowNodes) {
			if (flowNode instanceof Activity) {
				convertActivity((Activity)flowNode);
			} 
			else if (flowNode instanceof Event) {
				convertEvent((Event) flowNode);
			} 
			else if (flowNode instanceof Gateway) {
				// OR joins cannot be mapped at all!!!
				if(flowNode instanceof OrGateway) {
					throw new TransformationException(THE_GIVEN_PROCESS_MODEL_CONTAINS_AT_LEAST_ONE_OR_GATEWAY);
				}
				convertGateway((Gateway) flowNode);
			}
			//TODO handle further cases like sub processes, boundary events
		}	
	}
	
	/**
	 * Connect the already parsed {@link FlowNode}s according to the {@link ControlFlow}
	 * of the {@link ProcessModel} to convert. 
	 * The {@link ProcessModel} shall include {@link Gateway}s,
	 * that are either split or join and {@link Activity}s and {@link Event}s
	 * shall have only one incoming and one outgoing {@link ControlFlow} edge.
	 * @param edges {@link ControlFlow} edges to convert
	 */
	protected void convertControlFlowEdges(Collection<ControlFlow<FlowNode>> edges) {
		for(ControlFlow<FlowNode> f : edges) {
			Node source = this.nodeMapping.get(f.getSource());
			Node target = this.nodeMapping.get(f.getTarget());
	
			// the mapping of a flow between an XOR-split and an AND-join
			// might result in not semantically correct PetriNets
			if (f.getSource() instanceof XorGateway && f.getTarget() instanceof AndGateway) {			
				Transition t = new Transition();
				t.setId("xor/and_helper_" + this.getNextId());
				this.petriNet.addTransition(t);
				this.petriNet.addFlow(source, source);
				this.connectTwoTransitions(t, (Transition) target, "xor/and_helper_");
				continue;				
			}
			
			if ((source instanceof Place && target instanceof Transition)
					|| (source instanceof Transition && target instanceof Place)) {
				this.petriNet.addFlow(source, target);
			}
			else if ((source instanceof Place && target instanceof Place)) {
				this.connectTwoPlaces((Place) source, (Place) target, "helper_transition_for_edge_" + f.getId());
			}
			else if ((source instanceof Transition && target instanceof Transition)) {
				this.connectTwoTransitions((Transition) source, (Transition) target, "helper_place_for_edge_" + f.getId());
			}
		}
		addInitialAndFinalPlaces();
	}
	
	/**
	 * Adds an additional {@link Place} in front of each {@link Transition} 
	 * without incoming {@link Flow}.
	 */
	protected void addInitialAndFinalPlaces() {
		for(Transition t : this.petriNet.getSinkTransitions()) {
			Place p = new Place("final_for_" + t.getName());
			this.petriNet.addPlace(p);
			this.petriNet.addFlow(t, p);
		}
		for(Transition t : this.petriNet.getSourceTransitions()) {
			Place p = new Place("start_for_" + t.getName());
			this.petriNet.addPlace(p);
			this.petriNet.addFlow(p, t);
		}
	}

	/**
	 * Connects the two given {@link Place}s by inserting a silent {@link Transition} in the middle.
	 * @param source {@link Place} to start with
	 * @param target {@link Place} to end with
	 * @param label to use as id for {@link Transition} being inserted, because
	 * name must be the empty {@link String} to make the {@link Transition} silent.
	 */
	protected void connectTwoPlaces(Place source, Place target, String label) {
		Transition t = new Transition();
		t.setId(label + this.getNextId());
		this.petriNet.addTransition(t);
		this.petriNet.addFlow(source, t);
		this.petriNet.addFlow(t, target);
	}

	/**
	 * Connects the two given {@link Transition}s by inserting a {@link Place} in the middle.
	 * @param source {@link Transition} to start with
	 * @param target {@link Transition} to end with
	 * @param label to use for {@link Place} being inserted
	 */
	protected void connectTwoTransitions(Transition source, Transition target, String label) {
		Place p = new Place(label);
		p.setId(label + this.getNextId());
		this.petriNet.addPlace(p);
		this.petriNet.addFlow(source, p);
		this.petriNet.addFlow(p, target);
	}

	/**
	 * Converts the given {@link Activity} to a {@link Transition}.
	 * @param activity {@link Activity} to convert
	 */
	protected void convertActivity(Activity activity) {
		Transition t = new Transition();
		copyAttributes(activity, t);
		this.petriNet.addTransition(t);
		this.nodeMapping.put(activity, t);	
	}
	
	/**
	 * Converts the given {@link Event} to a {@link Transition}.
	 * @param event {@link Event} to convert
	 */
	protected void convertEvent(Event event) {
		Transition t = new Transition();
		copyAttributes(event, t);
		this.petriNet.addTransition(t);
		this.nodeMapping.put(event, t);
	}
}
