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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses;

import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;


/**
 * @author Tobias Metzke
 *
 */
public abstract class AbstractAnalysis implements IAnalysis {

	protected Map<String, AnalysisProcessModel> modelsToAnalyze;
	
	protected final String CSV_ITEMSEPARATOR = AnalysisConstant.ITEMSEPARATOR.getDescription();
	
	protected Map<String, AnalysisProcessModel> alreadyAnalyzedModels;
	
	protected Map<String, AnalysisProcessModel> analyzedModels;

	public AbstractAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		this(modelsToAnalyze, null);
	}
	
	public AbstractAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze, Map<String, AnalysisProcessModel> analyzedModels) {
		this.modelsToAnalyze = modelsToAnalyze;
		this.alreadyAnalyzedModels = analyzedModels;
	}
	
	@Override
	public String toResultCSVString() {
		if(analyzedModels == null) {
			analyzedModels = new HashMap<String, AnalysisProcessModel>();
			performAnalysis();
		}
		return getResultCSVString();
	}
	
	@Override
	public Map<String, AnalysisProcessModel> getAnalyzedModels() {
		if(analyzedModels == null) {
			analyzedModels = new HashMap<String, AnalysisProcessModel>();
			performAnalysis();
		}
		if(alreadyAnalyzedModels != null)
			return merge(analyzedModels,alreadyAnalyzedModels);
		return analyzedModels;
	}
	

	private Map<String, AnalysisProcessModel> merge(Map<String, AnalysisProcessModel> firstModelMap,
			Map<String, AnalysisProcessModel> secondModelMap) {
		Map<String, AnalysisProcessModel> newModelMap = new HashMap<>(secondModelMap);
		// add models of first collection to a new collection,
		// merge with models of second collection if necessary
		for (AnalysisProcessModel firstModel : firstModelMap.values()) {
			AnalysisProcessModel secondModel = secondModelMap.get(firstModel.getName());
			AnalysisProcessModel newModel;
			if (secondModel != null)
				newModel = new AnalysisProcessModel(firstModel, secondModel);
			else
				newModel = firstModel;
			newModelMap.put(newModel.getName(), newModel);
		}
		// add models of second collection
		// that haven't already been added before
		for (AnalysisProcessModel secondModel : secondModelMap.values()) {
			if (!newModelMap.containsKey(secondModel.getName())) {
				newModelMap.put(secondModel.getName(), secondModel);
			}
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
