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
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.extractor;

import java.util.logging.Logger;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.classification.StructuralModelChecker;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.IUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitData;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataClassification;

/**
 * @author Tobias Hoppe
 *
 */
public class ModelStructuringUnit implements IUnit<IUnitData<Object>, IUnitData<Object>> {

	/**
	 * logger used by this class
	 */
	private Logger logger = Logger.getLogger(PetriNetAnalyzerUnit.class.getName());
	
	@Override
	public IUnitData<Object> execute(IUnitData<Object> input) throws IllegalTypeException {
		if (input == null) {
			throw new IllegalArgumentException("Got an invalid null pointer input!");
		}
		if (input.getValue() == null || !(input.getValue() instanceof ProcessModel)){
			logger.warning("Got no model as input for structural model analysis!");
			return input;
		}
		if (input instanceof IUnitDataClassification<?>){
			//TODO check whether model can be structured and save structured model
			((IUnitDataClassification<Object>) input).setStructured(StructuralModelChecker.isStructured((ProcessModel) input.getValue()));			
		}
		return input;
	}

	@Override
	public String getName() {
		return "ModelStructuringUnit";
	}

	@Override
	public Class<?> getInputType() {
		return ProcessModel.class;
	}

	@Override
	public Class<?> getOutputType() {
		return ProcessModel.class;
	}

}
