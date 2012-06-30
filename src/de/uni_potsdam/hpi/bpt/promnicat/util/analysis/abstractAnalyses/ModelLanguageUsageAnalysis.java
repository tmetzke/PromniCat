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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class ModelLanguageUsageAnalysis extends AbstractAnalysis {

	private Map<String, Integer> results = new HashMap<String, Integer>();
	private Collection<AnalysisConstant> metrics; 
	
	/**
	 * @param modelsToAnalyze
	 */
	public ModelLanguageUsageAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze, Collection<AnalysisConstant> metrics) {
		super(modelsToAnalyze);
		this.metrics = metrics;
	}

	@Override
	protected void performAnalysis() {
		IAnalysis modelLanguage = AnalysisHelper.modelLanguageAnalysis(modelsToAnalyze);
		Map<String, AnalysisProcessModel> languageAnalyzedModels = modelLanguage.getAnalyzedModels();
		for (AnalysisProcessModel model : languageAnalyzedModels.values()) {
			AnalysisConstant behavior = AnalysisConstant.NONE;
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				Collection<String> languageElements = revision.getMetrics().keySet();
				behavior = findBehavior(languageElements,behavior);
			}
			String behaviorString = behavior.getDescription();
			int oldValue = results.containsKey(behaviorString) ? results.get(behaviorString) : 0;
			results.put(behaviorString, ++oldValue);
		}
	}
	
	private static AnalysisConstant findBehavior(Collection<String> languageElements,
			AnalysisConstant behavior) {
		AnalysisConstant newBehavior = behavior;
		String controlConstant = AnalysisConstant.CONTROL_FLOW.getDescription();
		String dataConstant = AnalysisConstant.DATA_FLOW.getDescription();
		String orgaConstant = AnalysisConstant.ORGANISATION.getDescription();
		
		switch (behavior) {
		case CONTROL_FLOW:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstant.CONTROL_ORGA;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstant.CONTROL_DATA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
	
		case DATA_FLOW:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstant.DATA_CONTROL;
			else if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstant.DATA_ORGA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
			
		case ORGANISATION:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstant.ORGA_CONTROL;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstant.ORGA_DATA;
			else
				break;
			newBehavior = findBehavior(languageElements, newBehavior);
			break;
			
		case CONTROL_ORGA:
			if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstant.CONTROL_ORGA_DATA;
			break;
			
		case CONTROL_DATA:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstant.CONTROL_DATA_ORGA;
			break;
			
		case DATA_CONTROL:
			if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstant.DATA_CONTROL_ORGA;
			break;
			
		case DATA_ORGA:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstant.DATA_ORGA_CONTROL;
			break;
			
		case ORGA_CONTROL:
			if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstant.ORGA_CONTROL_DATA;
			break;
			
		case ORGA_DATA:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstant.ORGA_DATA_CONTROL;
			break;
			
		case NONE:
			if (languageElements.contains(controlConstant))
				newBehavior = AnalysisConstant.CONTROL_FLOW;
			else if (languageElements.contains(orgaConstant))
				newBehavior = AnalysisConstant.ORGANISATION;
			else if (languageElements.contains(dataConstant))
				newBehavior = AnalysisConstant.DATA_FLOW;
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
		for (AnalysisConstant languageConstant : metrics)
			if (results.containsKey(languageConstant.getDescription()))
				builder.append(languageConstant.getDescription() + CSV_ITEMSEPARATOR);
		builder.append("\n");
		for (AnalysisConstant languageConstant : metrics)
			if (results.containsKey(languageConstant.getDescription()))
				builder.append(results.get(languageConstant.getDescription()) + CSV_ITEMSEPARATOR);
		return builder.toString();
	}

}
