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

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IMetricsAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class ModelGrowthAnalysis extends AbstractAnalysis {

	private int growingModels = 0;
	private int numberOfModels = 0;
	
	public ModelGrowthAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	protected void performAnalysis() {
		IMetricsAnalysis differenceAnalysis = AnalysisHelper.analyzeDifferencesInMetrics(modelsToAnalyze, false);
		Map<String, AnalysisProcessModel> differenceAnalyzedModels = differenceAnalysis.getAnalyzedModels();
		
		// continuously growing models
		numberOfModels = differenceAnalyzedModels.size();
		for (AnalysisProcessModel model : differenceAnalyzedModels.values())
			if (model.isGrowing()) growingModels++;
	}
	
	@Override
	protected String getResultCSVString() {
		StringBuilder builder = new StringBuilder()
			.append(AnalysisConstant.NUM_MODELS.getDescription() + CSV_ITEMSEPARATOR)
			.append(AnalysisConstant.NUM_GROWING.getDescription() + CSV_ITEMSEPARATOR)
			.append(AnalysisConstant.NUM_NOT_GROWING.getDescription())
			.append("\n")
			.append(numberOfModels + CSV_ITEMSEPARATOR)
			.append(growingModels + CSV_ITEMSEPARATOR)
			.append(numberOfModels - growingModels)
			.append("\n\n");			
		return builder.toString();
	}

}
