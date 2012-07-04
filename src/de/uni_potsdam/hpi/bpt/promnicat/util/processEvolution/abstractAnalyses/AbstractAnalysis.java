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
package de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.abstractAnalyses;

import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisConstants;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.api.IAnalysis;


/**
 * @author Tobias Metzke
 *
 */
public abstract class AbstractAnalysis implements IAnalysis {

	protected Map<String, ProcessEvolutionModel> modelsToAnalyze;
	
	protected final String CSV_ITEMSEPARATOR = AnalysisConstants.ITEMSEPARATOR.getDescription();
	
	protected Map<String, ProcessEvolutionModel> alreadyAnalyzedModels;
	
	protected Map<String, ProcessEvolutionModel> analyzedModels;

	public AbstractAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze) {
		this(modelsToAnalyze, null);
	}
	
	public AbstractAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze, Map<String, ProcessEvolutionModel> analyzedModels) {
		this.modelsToAnalyze = modelsToAnalyze;
		this.alreadyAnalyzedModels = analyzedModels;
	}
	
	@Override
	public String toResultCSVString() {
		if(analyzedModels == null) {
			analyzedModels = new HashMap<String, ProcessEvolutionModel>();
			performAnalysis();
		}
		return getResultCSVString();
	}
	
	@Override
	public Map<String, ProcessEvolutionModel> getAnalyzedModels() {
		if(analyzedModels == null) {
			analyzedModels = new HashMap<String, ProcessEvolutionModel>();
			performAnalysis();
		}
		if(alreadyAnalyzedModels != null)
			return merge(analyzedModels,alreadyAnalyzedModels);
		return analyzedModels;
	}
	

	private Map<String, ProcessEvolutionModel> merge(Map<String, ProcessEvolutionModel> firstModelMap,
			Map<String, ProcessEvolutionModel> secondModelMap) {
		Map<String, ProcessEvolutionModel> newModelMap = new HashMap<>(secondModelMap);
		// add models of first collection to a new collection,
		// merge with models of second collection if necessary
		for (ProcessEvolutionModel firstModel : firstModelMap.values()) {
			ProcessEvolutionModel secondModel = secondModelMap.get(firstModel.getName());
			ProcessEvolutionModel newModel;
			if (secondModel != null)
				newModel = new ProcessEvolutionModel(firstModel, secondModel);
			else
				newModel = firstModel;
			newModelMap.put(newModel.getName(), newModel);
		}
		// add models of second collection
		// that haven't already been added before
		for (ProcessEvolutionModel secondModel : secondModelMap.values()) {
			if (!newModelMap.containsKey(secondModel.getName()))
				newModelMap.put(secondModel.getName(), secondModel);
		}
		return newModelMap;
	}

	/**
	 * executes the analysis method every subclass defines itself
	 */
	protected abstract void performAnalysis();
	
	/**
	 * converts the analysis results into a proper CSV format 
	 * @return the CSV format of the results as String
	 */
	protected abstract String getResultCSVString();
}
