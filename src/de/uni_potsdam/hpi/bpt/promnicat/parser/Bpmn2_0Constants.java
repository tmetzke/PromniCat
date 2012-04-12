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
package de.uni_potsdam.hpi.bpt.promnicat.parser;

/**
 * Constants for id matching of a JSON Diagram for Bpmn 2.0
 * @author Cindy Fähnrich
 *
 */
public class Bpmn2_0Constants extends Bpmn1_1Constants {
		
	/**
	 * Constructor; here, all constants differing from Bpmn 1.1 are reset to a new value
	 */
	public Bpmn2_0Constants(){
		
		ENTITY_SUBPROCESS_EVENT = "Event";
	
		ENTITY_GATEWAY_AND = "ParallelGateway";
		ENTITY_GATEWAY_OR = "InclusiveGateway";
		ENTITY_GATEWAY_XOR = "Exclusive_Databased_Gateway";
		ENTITY_GATEWAY_EVENTBASED = "EventbasedGateway";
		ENTITY_GATEWAY_ALTERNATIVE = "ComplexGateway";
		
		PROPERTY_ISCOMPENSATION = "isforcompensation";
		PROPERTY_MI_ORDER = "looptype";
	}
}

