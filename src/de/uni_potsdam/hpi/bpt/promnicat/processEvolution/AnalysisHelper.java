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
package de.uni_potsdam.hpi.bpt.promnicat.processEvolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.HighLevelAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses.AdditionsDeletionsAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses.DifferenceAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses.ModelLanguageAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses.MovedElementsAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.abstractAnalyses.metricAnalyses.RelativeDifferenceAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.processEvolution.api.IAnalysis;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;

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
	public static IAnalysis analyzeDifferencesInMetrics(Map<String, ProcessEvolutionModel> models, boolean relative) {
		IAnalysis differenceAnalysis = relative ? 
				new RelativeDifferenceAnalysis(models, getProcessModelMetrics()) :
				new DifferenceAnalysis(models, getProcessModelMetrics());
		return differenceAnalysis;
	}

	public static IAnalysis analyzeAdditionsAndDeletions(Map<String, ProcessEvolutionModel> models, boolean includeSubprocesses) {
		return new AdditionsDeletionsAnalysis(models, getIndividualMetrics(), includeSubprocesses);
	}
	
	public static IAnalysis modelLanguageAnalysis(Map<String, ProcessEvolutionModel> models) {
		return new ModelLanguageAnalysis(models);
	}

	public static IAnalysis analyzeElementMovements(
			Map<String, ProcessEvolutionModel> modelsToBeAnalyzed) {
		return new MovedElementsAnalysis(modelsToBeAnalyzed);
	}
	
	public static IAnalysis analyzeElementMovements(
			Map<String, ProcessEvolutionModel> modelsToBeAnalyzed,
			Map<String, ProcessEvolutionModel> analyzedModels) {
		return new MovedElementsAnalysis(modelsToBeAnalyzed, analyzedModels);
	}

	/**
	 * further analyze the already analyzed models and try to find high-level
	 * results like the number of continuously growing models
	 * @param analyzedModels
	 * @throws IOException
	 */
	public static IAnalysis highLevelAnalysis(Map<String, ProcessEvolutionModel> models, boolean includeSubprocesses) throws IOException {
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
	
	public static Collection<AnalysisConstants> getIndividualMetrics() {
		Collection<AnalysisConstants> individualMetrics = new ArrayList<>();
		Collections.addAll(individualMetrics,
				AnalysisConstants.EVENTS, AnalysisConstants.ACTIVITIES, 
				AnalysisConstants.GATEWAYS, AnalysisConstants.DOCUMENTS, 
				AnalysisConstants.ROLES, AnalysisConstants.EDGES);
		return individualMetrics;
	}
	
	public static Collection<AnalysisConstants> getModelLanguageMetrics() {
		Collection<AnalysisConstants> languageMetrics = new ArrayList<>();
		Collections.addAll(languageMetrics,
				AnalysisConstants.CONTROL_ORGA_DATA, AnalysisConstants.CONTROL_DATA_ORGA,
				AnalysisConstants.DATA_CONTROL_ORGA, AnalysisConstants.DATA_ORGA_CONTROL, 
				AnalysisConstants.ORGA_CONTROL_DATA, AnalysisConstants.ORGA_DATA_CONTROL,
				AnalysisConstants.CONTROL_DATA, AnalysisConstants.CONTROL_ORGA,
				AnalysisConstants.DATA_CONTROL, AnalysisConstants.DATA_ORGA,
				AnalysisConstants.ORGA_CONTROL, AnalysisConstants.ORGA_DATA,
				AnalysisConstants.CONTROL_FLOW, AnalysisConstants.DATA_FLOW,
				AnalysisConstants.ORGANISATION);
		return languageMetrics;
	}
}
