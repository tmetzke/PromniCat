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
import org.jbpt.pm.ProcessModel;

/**
 * @author Tobias Hoppe
 *
 */
public interface IUnitDataClassification<V extends Object> extends IUnitDataJbpt<V> {

	/**
	 * @return the {@link PetriNet} generated from the {@link ProcessModel}
	 * of this {@link UnitDataClassification}.
	 */
	public PetriNet getPetriNet();
	
	/**
	 * @return <code>true</code> if the {@link ProcessModel} is
	 * conform to the descriptive modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * <code>false</code> otherwise.
	 */
	public boolean getDescriptiveModelingConformance();
	
	/**
	 * @return <code>true</code> if the {@link ProcessModel} is
	 * conform to the analytic modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * <code>false</code> otherwise.
	 */
	public boolean getAnalyticModelingConformance();
	
	/**
	 * @return <code>true</code> if the {@link ProcessModel} is
	 * conform to the common executable modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * <code>false</code> otherwise.
	 */
	public boolean getCommonExecutableModelingConformance();
	
	/**
	 * Set the {@link PetriNet} generated from the {@link ProcessModel}
	 * of this {@link UnitDataClassification}.
	 * @param net
	 */
	public void setPetriNet(PetriNet net);
	
	/**
	 * Set to <code>true</code>, if the analyzed {@link ProcessModel} is
	 * conform to the descriptive modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * @param isConform
	 */
	public void setDescriptiveModelingConformance(boolean isConform);
	
	/**
	 * Set to <code>true</code>, if the analyzed {@link ProcessModel} is
	 * conform to the analytic modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * @param isConform
	 */
	public void setAnalyticModelingConformance(boolean isConform);
	
	/**
	 * Set to <code>true</code>, if the analyzed {@link ProcessModel} is
	 * conform to the common executable modeling specification as defined
	 * in the BPMN version 2.0 specification.
	 * @param isConform
	 */
	public void setCommonExecutableModelingConformance(boolean isConform);

	/**
	 * @param itemseparator char(s) to use for separation of result items
	 * @return a CSV-Representation of this {@link UnitDataClassification}.
	 */
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
