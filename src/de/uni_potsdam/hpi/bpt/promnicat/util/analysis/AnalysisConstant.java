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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis;

/**
 * @author Tobias Metzke
 *
 */
public enum AnalysisConstant {

	/**
	 * the key for the number of models that are analyzed
	 */
	NUM_MODELS ("number of models"),

	/**
	 * the key for the number of models that grow continuously
	 */
	NUM_GROWING ("continuously growing"),

	/**
	 * the key for the number of models that do not grow continuously
	 */
	NUM_NOT_GROWING ("not always growing"),
	
	UNALTERING_REVISIONS ("unaltering Revisions"),

	ALTERING_REVISIONS ("altering revisions"),

	NUM_REVISIONS ("number of revisions"),

	LOWER ("lower"),

	SAME ("same"),

	HIGHER ("higher"), 
	
	ADD_DELETE ("add_delete"), 
	
	ACTIVITIES ("activities"),
	
	EDGES ("edges"),
	
	GATEWAYS ("gateways"),
	
	ROLES ("roles"), 
	
	DOCUMENTS ("documents"),
	
	ADDITIONS ("additions"),
	
	DELETIONS ("deletions"), 
	
	EVENTS ("events");
	
	private String stringRepresentation;
	
	private AnalysisConstant(String name) {
		this.stringRepresentation = name;
	}
	
	public String getDescription() {
		return this.stringRepresentation;
	}
}
