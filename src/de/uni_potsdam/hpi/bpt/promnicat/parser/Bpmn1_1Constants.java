/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General public static  License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General public static  License for more details.
 * 
 * You should have received a copy of the GNU General public static  License
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
	public static  String ENTITY_TASK = "Task";
	public static  String ENTITY_SUBPROCESS = "Subprocess";
	public static  String ENTITY_SUBPROCESS_EVENT = " "; //no eventbased subprocess exists in Bpmn 1.1
	public static  String ENTITY_SUBPROCESS_COLLAPSED = "Collapsed";
	
	/**
	 * Gateways
	 */
	public static  String ENTITY_GATEWAY_AND = "AND_Gateway";
	public static  String ENTITY_GATEWAY_OR = "OR_Gateway";
	public static  String ENTITY_GATEWAY_XOR = "Exclusive_Databased_Gateway";
	public static  String ENTITY_GATEWAY_EVENTBASED = "Eventbased_Gateway";
	public static  String ENTITY_GATEWAY_ALTERNATIVE = "Complex_Gateway";
	
	/**
	 * Events - Start
	 */
	public static  String ENTITY_EVENT_START = "Start";
	
	/**
	 * Events - Intermediate
	 * catching and throwing events contain this term also, so it is checked only at the end
	 */
	public static  String ENTITY_EVENT_INTERMEDIATE = "Intermediate"; 
	public static  String ENTITY_EVENT_CATCHING = "Catching";
	public static  String ENTITY_EVENT_THROWING = "Throwing";
	
	/**
	 * Events - End
	 */
	public static  String EVENT_END = "End";
	
	/**
	 * Resources and Data
	 */
	public static  String ENTITY_POOL = "Pool";
	public static  String ENTITY_LANE = "Lane";
	public static  String ENTITY_DATA = "DataObject";
	
	/**
	 * Edges
	 */
	public static  String ENTITY_SEQUENCEFLOW = "SequenceFlow";
	public static  String ENTITY_ASSOCIATION = "Association";
	public static  String ENTITY_MESSAGEFLOW = "MessageFlow";
	
	/**
	 * Json Property names
	 */
	public static  String PROPERTY_ISCOMPENSATION = "iscompensation";
	public static  String PROPERTY_LOOPTYPE = "looptype";
	public static  String PROPERTY_MI_ORDER = "mi_ordering";
	public static  String PROPERTY_ISADHOC= "isadhoc";
	public static  String PROPERTY_ADHOC_ORDER = "adhocordering";
	public static  String PROPERTY_CONDITION_TYPE = "conditiontype";
	public static  String PROPERTY_CONDITION_EXPRESSION = "conditionexpression";
	public static  String PROPERTY_NAME = "name";
	public static  String PROPERTY_DESCRIPTION = "description";
	public static  String PROPERTY_TITLE = "title";
	
	/**
	 * Json Property values
	 */
	public static  String VALUE_MULTIINSTANCE = "MultiInstance";
	public static  String VALUE_UNDIRECTED = "Undirected";
	public static  String VALUE_UNIDIRECTED = "Unidirectional";
	public static  String VALUE_BIDIRECTED = "Bidirected";
	public static  String VALUE_DEFAULT = "Default";
	public static  String VALUE_SEQUENTIAL = "Sequential";
	public static  String VALUE_TRUE = "true";
	public static  String VALUE_STANDARD = "Standard";
	public static  String VALUE_NONE = "None";
	   
}
