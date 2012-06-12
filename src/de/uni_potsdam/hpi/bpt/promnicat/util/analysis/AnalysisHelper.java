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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses.HighLevelAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.api.IMetricsAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses.AdditionsDeletionsAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses.DifferenceAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses.ModelLanguageAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses.RelativeDifferenceAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class AnalysisHelper {

	/**
	 * analysis of the analyzed models.
	 * @param models the models to be analyzed further
	 * @param relative flag to determine whether the values shall be relative
	 * to the absolute old number (<code>true</code>) or absolute (<code>false</code>)
	 * @return the analyzed models, their revisions and their values
	 */
	public static IMetricsAnalysis analyzeDifferencesInMetrics(Map<String, AnalysisProcessModel> models, boolean relative) {
		IMetricsAnalysis differenceAnalysis = relative ? 
				new RelativeDifferenceAnalysis(models, getProcessModelMetrics()) :
				new DifferenceAnalysis(models, getProcessModelMetrics());
		return differenceAnalysis;
	}

	public static IMetricsAnalysis analyzeAdditionsAndDeletions(Map<String, AnalysisProcessModel> models, boolean includeSubprocesses) {
		return new AdditionsDeletionsAnalysis(models, getIndividualMetrics(), includeSubprocesses);
	}
	
	public static IMetricsAnalysis modelLanguageAnalysis(Map<String, AnalysisProcessModel> models) {
		return new ModelLanguageAnalysis(models);
	}

	/**
	 * further analyze the already analyzed models and try to find high-level
	 * results like the number of continuously growing models
	 * @param analyzedModels
	 * @throws IOException
	 */
	public static IAnalysis highLevelAnalysis(Map<String, AnalysisProcessModel> models, boolean includeSubprocesses) throws IOException {
		return new HighLevelAnalysis(models, includeSubprocesses);
	}

	/*
	 * ------------------------------------------------------------------------
	 * GETTERS FOR METRICS COLLECTIONS
	 * ------------------------------------------------------------------------
	 */
	/**
	 * access to the herein defined metrics that are analyzed per model revision
	 * and displayed in analysis results
	 * @return the metrics all model revisions are analyzed by
	 */
	public static Collection<METRICS> getProcessModelMetrics() {
		Collection<METRICS> processModelMetrics = new ArrayList<>();
		Collections.addAll(processModelMetrics, 
				METRICS.NUM_EVENTS,	METRICS.NUM_ACTIVITIES, 
				METRICS.NUM_GATEWAYS,METRICS.NUM_NODES, 
				METRICS.NUM_EDGES, METRICS.NUM_ROLES,
				METRICS.NUM_DATA_NODES);
		return processModelMetrics;
	}
	
	public static Collection<AnalysisConstant> getIndividualMetrics() {
		Collection<AnalysisConstant> individualMetrics = new ArrayList<>();
		Collections.addAll(individualMetrics,
				AnalysisConstant.EVENTS, AnalysisConstant.ACTIVITIES, 
				AnalysisConstant.GATEWAYS, AnalysisConstant.DOCUMENTS, 
				AnalysisConstant.ROLES, AnalysisConstant.EDGES);
		return individualMetrics;
	}
	
	public static Collection<AnalysisConstant> getModelLanguageMetrics() {
		Collection<AnalysisConstant> languageMetrics = new ArrayList<>();
		Collections.addAll(languageMetrics,
				AnalysisConstant.CONTROL_ORGA_DATA, AnalysisConstant.CONTROL_DATA_ORGA,
				AnalysisConstant.DATA_CONTROL_ORGA, AnalysisConstant.DATA_ORGA_CONTROL, 
				AnalysisConstant.ORGA_CONTROL_DATA, AnalysisConstant.ORGA_DATA_CONTROL,
				AnalysisConstant.CONTROL_DATA, AnalysisConstant.CONTROL_ORGA,
				AnalysisConstant.DATA_CONTROL, AnalysisConstant.DATA_ORGA,
				AnalysisConstant.ORGA_CONTROL, AnalysisConstant.ORGA_DATA,
				AnalysisConstant.CONTROL_FLOW, AnalysisConstant.DATA_FLOW,
				AnalysisConstant.ORGANISATION);
		return languageMetrics;
	}
}
