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
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData;

import org.jbpt.petri.PetriNet;

/**
 * @author Tobias Hoppe
 *
 */
public interface IUnitDataClassification<V extends Object> extends IUnitDataJbpt<V> {

	public PetriNet getPetriNet();
	
	public boolean getDescriptiveModelingConformance();
	
	public boolean getAnalyticModelingConformance();
	
	public boolean getCommonExecutableModelingConformance();
	
	public void setPetriNet(PetriNet net);
	
	public void setDescriptiveModelingConformance(boolean isConform);
	
	public void setAnalyticModelingConformance(boolean isConform);
	
	public void setCommonExecutableModelingConformance(boolean isConform);

	public String toCsv(String itemseparator);
	
	/**
	 * @return the modelPath
	 */
	public String getModelPath();

	/**
	 * @param modelPath the modelPath to set
	 */
	public void setModelPath(String modelPath);
}
