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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisConstants;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class ModelLanguageUsageAnalysis extends AbstractAnalysis {

	private Map<String, Integer> results = new HashMap<String, Integer>();
	private Collection<AnalysisConstants> metrics; 
	
	/**
	 * @param modelsToAnalyze
	 */
	public ModelLanguageUsageAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze, Map<String, ProcessEvolutionModel> analyzedModels, Collection<AnalysisConstants> metrics) {
		super(modelsToAnalyze, analyzedModels);
		this.metrics = metrics;
	}
	
	public ModelLanguageUsageAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze, Collection<AnalysisConstants> metrics) {
		this(modelsToAnalyze, null, metrics);
	}

	@Override
	protected void performAnalysis() {
		IAnalysis modelLanguage = AnalysisHelper.modelLanguageAnalysis(modelsToAnalyze);
		Map<String, ProcessEvolutionModel> languageAnalyzedModels = modelLanguage.getAnalyzedModels();
		for (ProcessEvolutionModel model : languageAnalyzedModels.values()) {
			AnalysisConstants behavior = AnalysisConstants.NONE;
			for (ProcessEvolutionModelRevision revision : model.getRevisions().values()) {
				Collection<String> languageElements = revision.getMetrics().keySet();
				behavior = findBehavior(languageElements,behavior);
			}
			String behaviorString = behavior.getDescription();
			int oldValue = results.containsKey(behaviorString) ? results.get(behaviorString) : 0;
			results.put(behaviorString, ++oldValue);
		}
	}
	
	private static AnalysisConstants findBehavior(Collection<String> languageElements,
			AnalysisConstants behavior) {
		AnalysisConstants newBehavior = behavior;
		String controlConstant = AnalysisConstants.CONTROL_FLOW.getDescription();
		String dataConstant = AnalysisConstants.DATA_FLOW.getDescription();
		String orgaConstant = AnalysisConstants.ORGANISATION.getDescription();
		
		switch (behavior) {
		case CONTROL_FLOW:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstants.CONTROL_ORGA;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstants.CONTROL_DATA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
	
		case DATA_FLOW:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstants.DATA_CONTROL;
			else if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstants.DATA_ORGA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
			
		case ORGANISATION:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstants.ORGA_CONTROL;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstants.ORGA_DATA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
			
		case CONTROL_ORGA:
			if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstants.CONTROL_ORGA_DATA;
			break;
			
		case CONTROL_DATA:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstants.CONTROL_DATA_ORGA;
			break;
			
		case DATA_CONTROL:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstants.DATA_CONTROL_ORGA;
			break;
			
		case DATA_ORGA:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstants.DATA_ORGA_CONTROL;
			break;
			
		case ORGA_CONTROL:
			if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstants.ORGA_CONTROL_DATA;
			break;
			
		case ORGA_DATA:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstants.ORGA_DATA_CONTROL;
			break;
			
		case NONE:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstants.CONTROL_FLOW;
			else if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstants.ORGANISATION;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstants.DATA_FLOW;
			else
				break;
			
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
		
		default:
			break;
		}
		return newBehavior;
	}

	@Override
	protected String getResultCSVString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n");
		for (AnalysisConstants languageConstant : metrics)
			if (results.containsKey(languageConstant.getDescription()))
				builder.append(languageConstant.getDescription() + CSV_ITEMSEPARATOR);
		builder.append("\n");
		for (AnalysisConstants languageConstant : metrics)
			if (results.containsKey(languageConstant.getDescription()))
				builder.append(results.get(languageConstant.getDescription()) + CSV_ITEMSEPARATOR);
		return builder.toString();
	}

}
