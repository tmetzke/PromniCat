/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General public   License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General public   License for more details.
 * 
 * You should have received a copy of the GNU General public   License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_potsdam.hpi.bpt.promnicat.parser;


/**
 * Constants for id matching of a JSON Diagram for Bpmn 1.1
 * @author Cindy Fähnrich
 *
 */
public class Bpmn1_1Constants {

	/**
	 * Activities
	 */
	public   String ENTITY_TASK = "Task";
	public   String ENTITY_SUBPROCESS = "Subprocess";
	public   String ENTITY_SUBPROCESS_EVENT = " "; //no eventbased subprocess exists in Bpmn 1.1
	public   String ENTITY_SUBPROCESS_COLLAPSED = "Collapsed";
	
	/**
	 * Gateways
	 */
	public   String ENTITY_GATEWAY_AND = "AND_Gateway";
	public   String ENTITY_GATEWAY_OR = "OR_Gateway";
	public   String ENTITY_GATEWAY_XOR = "Exclusive_Databased_Gateway";
	public   String ENTITY_GATEWAY_EVENTBASED = "Eventbased_Gateway";
	public   String ENTITY_GATEWAY_ALTERNATIVE = "Complex_Gateway";
	
	/**
	 * Events - Start
	 */
	public   String ENTITY_EVENT_START = "Start";
	
	/**
	 * Events - Intermediate
	 * catching and throwing events contain this term also, so it is checked only at the end
	 */
	public   String ENTITY_EVENT_INTERMEDIATE = "Intermediate"; 
	public   String ENTITY_EVENT_CATCHING = "Catching";
	public   String ENTITY_EVENT_THROWING = "Throwing";
	
	/**
	 * Events - End
	 */
	public   String EVENT_END = "End";
	
	/**
	 * Resources and Data
	 */
	public   String ENTITY_POOL = "Pool";
	public   String ENTITY_LANE = "Lane";
	public   String ENTITY_DATA = "DataObject";
	
	/**
	 * Edges
	 */
	public   String ENTITY_SEQUENCEFLOW = "SequenceFlow";
	public   String ENTITY_ASSOCIATION = "Association";
	public   String ENTITY_MESSAGEFLOW = "MessageFlow";
	
	/**
	 * Json Property names
	 */
	public   String PROPERTY_ISCOMPENSATION = "iscompensation";
	public   String PROPERTY_LOOPTYPE = "looptype";
	public   String PROPERTY_MI_ORDER = "mi_ordering";
	public   String PROPERTY_ISADHOC= "isadhoc";
	public   String PROPERTY_ADHOC_ORDER = "adhocordering";
	public   String PROPERTY_CONDITION_TYPE = "conditiontype";
	public   String PROPERTY_CONDITION_EXPRESSION = "conditionexpression";
	public   String PROPERTY_NAME = "name";
	public   String PROPERTY_DESCRIPTION = "description";
	public   String PROPERTY_TITLE = "title";
	
	/**
	 * Json Property values
	 */
	public   String VALUE_MULTIINSTANCE = "MultiInstance";
	public   String VALUE_UNDIRECTED = "Undirected";
	public   String VALUE_UNIDIRECTED = "Unidirectional";
	public   String VALUE_BIDIRECTED = "Bidirected";
	public   String VALUE_DEFAULT = "Default";
	public   String VALUE_SEQUENTIAL = "Sequential";
	public   String VALUE_TRUE = "true";
	public   String VALUE_STANDARD = "Standard";
	public   String VALUE_NONE = "None";
	   
}
