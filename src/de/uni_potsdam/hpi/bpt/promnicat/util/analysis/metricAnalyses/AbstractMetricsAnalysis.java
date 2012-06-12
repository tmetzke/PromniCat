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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses;

import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics.ProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses.AbstractAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IMetricsAnalysis;

/**
 * @author Tobias Metzke
 * 
 */
public abstract class AbstractMetricsAnalysis extends AbstractAnalysis implements IMetricsAnalysis{

	protected Map<String, AnalysisProcessModel> analyzedModels = new HashMap<String, AnalysisProcessModel>();

	public AbstractMetricsAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	public Map<String, AnalysisProcessModel> getAnalyzedModels() {
		performAnalysis();
		return analyzedModels;
	}
	

	@Override
	protected String getResultCSVString() {
		StringBuilder resultStringBuilder = new StringBuilder(addCSVHeader());
		// collect result from each model
		for (AnalysisProcessModel model : analyzedModels.values())
			resultStringBuilder.append(toCsvString(model));
		return resultStringBuilder.toString();
	}
	
	/**
	 * add the specific header to the analysis files that includes
	 * the model path, revision and the metrics of this revision
	 * @return a String representation of the process model metrics
	 * separated by the {@link ProcessMetrics#CSV_ITEMSEPARATOR}.
	 */
	protected abstract String addCSVHeader();
	
	/**
	 * helper method that puts the real data of the metrics into format.
	 * every model revision is put into a new line with the according metrics
	 * @param model that should be displayed in CSV format
	 * @return a String representation of the formatted model results
	 */
	protected abstract String toCsvString(AnalysisProcessModel model);
}
