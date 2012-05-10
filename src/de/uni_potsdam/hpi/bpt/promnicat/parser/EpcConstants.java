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
 * Constants for id matching of a Json Diagram for Epc
 * @author Cindy Fähnrich
 *
 */
public class EpcConstants {

	/**
	 * Notation elements
	 */
	public String ENTITY_EVENT = "Event";
	public String ENTITY_FUNCTION = "Function";
	public String ENTITY_XORCONNECTOR = "XorConnector";
	public String ENTITY_ORCONNECTOR = "OrConnector";
	public String ENTITY_ANDCONNECTOR = "AndConnector";
	public String ENTITY_CONTROLFLOW = "ControlFlow";
	public String ENTITY_PROCESSINTERFACE = "ProcessInterface";
	public String ENTITY_ORGANIZATION = "Organization";
	public String ENTITY_ORGANIZATIONUNIT = "OrganizationUnit";
	public String ENTITY_POSITION = "Position";
	public String ENTITY_DATA = "Data";
	public String ENTITY_RELATION = "Relation";
	
	/**
	 * Property names in Json
	 */
	public String PROPERTY_INFOFLOW = "informationflow";
	public String PROPERTY_TITLE = "title";
	public String PROPERTY_DESCRIPTION = "description";
	public String PROPERTY_ENTRY = "entry";
	
	/**
	 * Property values in Json
	 */
	public String VALUE_FALSE = "False";
	public String VALUE_TRUEINVERSE = "TrueInverse";
	
}
