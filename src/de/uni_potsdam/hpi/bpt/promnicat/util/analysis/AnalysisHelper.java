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
import java.util.HashMap;
import java.util.Map;

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
	public static Map<String,AnalysisProcessModel> analyzeMetrics(Map<String, AnalysisProcessModel> models, boolean relative) {
		// a new data structure to store the results in
		Map<String,AnalysisProcessModel> newModels = new HashMap<>();
		
		for (AnalysisProcessModel model : models.values()) {
			AnalysisProcessModel newModel = performDifferenceAnalysisFor(model, relative);
			
			newModels.put(model.getName(), newModel);
		}
		return newModels;
	}
	
	/**
	 * per model revision the difference to the previous revision
	 * is stored for all metrics
	 * @param relative flag to determine whether the values shall be relative
	 * @param model the model to be analyzed
	 * @return the analyzed model
	 */
	private static AnalysisProcessModel performDifferenceAnalysisFor(
			AnalysisProcessModel model, boolean relative) {
		
		AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
		Map<METRICS, Double> oldValues = getInitialValues();
		// perform the analysis of differences for every revision and metric
		for (AnalysisModelRevision revision : model.getRevisions().values()) {
			AnalysisModelRevision newRevision = new AnalysisModelRevision(revision.getRevisionNumber());
			for (METRICS metric : getProcessModelMetrics()) {
				double actualValue = revision.get(metric);
				double oldValue = oldValues.get(metric);
				double difference = calculateDifference(metric, actualValue, oldValue, relative);
				// save the new value as back-reference for the next revision
				oldValues.put(metric,actualValue);
				newRevision.add(metric, difference);
				// if a metric is actually lower than in the previous revision,
				// the model is not growing continuously
				if (difference < 0) newModel.setGrowing(false);
			}
			newModel.add(newRevision);
		}
		return newModel;
	}
	
	/**
	 * initialize a first collection of metrics zero-values to have a starting
	 * point for the first revision of a model to be compared to
	 * @return
	 */
	private static Map<METRICS, Double> getInitialValues() {
		Map<METRICS, Double> oldValues = new HashMap<>();
		for (METRICS metric : getProcessModelMetrics())
			oldValues.put(metric, new Double(0));
		return oldValues;
	}

	/**
	 * execution of the difference analysis
	 * @param metric the metric to be analyzed
	 * @param revision the actual revision containing its metric values
	 * @param oldValues the previous set of values
	 * @param relative
	 * @return
	 */
	private static double calculateDifference(METRICS metric, double actualValue, double oldValue, boolean relative) {
		double divisor = 1;
		int factor = 1;
		if (relative) {
			divisor = oldValue == 0 ? actualValue : oldValue;
			if (divisor == 0) divisor = 1;
			factor = 100;
		}
		double difference = (actualValue - oldValue) * factor / divisor;
		return difference;
	}
	
	/**
	 * access to the herein defined metrics that are analyzed per model revision
	 * and displayed in analysis results
	 * @return the metrics all model revisions are analyzed by
	 */
	public static Collection<METRICS> getProcessModelMetrics() {
		ArrayList<METRICS> processModelMetrics = new ArrayList<>();
		Collections.addAll(processModelMetrics, 
				METRICS.NUM_EVENTS,	METRICS.NUM_ACTIVITIES, 
				METRICS.NUM_GATEWAYS,METRICS.NUM_NODES, 
				METRICS.NUM_EDGES, METRICS.NUM_ROLES);
		return processModelMetrics;
	}
	
	/**
	 * further analyze the already analyzed models and try to find high-level
	 * results like the number of continuously growing models
	 * @param analyzedModels
	 * @throws IOException
	 */
	public static Map<String, Integer> highLevelAnalysis(Map<String, AnalysisProcessModel> analyzedModels) throws IOException {
		// continuously growing models
		Map<String, Integer> features = new HashMap<>();
		int numberOfModels = analyzedModels.size();
		int growingModels = 0;
		for (AnalysisProcessModel model : analyzedModels.values())
			if (model.isGrowing()) growingModels++;
		features.put(AnalysisConstant.NUM_MODELS.getString(), numberOfModels);
		features.put(AnalysisConstant.NUM_GROWING.getString(), growingModels);
		features.put(AnalysisConstant.NUM_NOT_GROWING.getString(), numberOfModels - growingModels);
		
		// number of revisions that higher, lower and do not change the numbers of a metric
		for (METRICS metric : getProcessModelMetrics()) {
			int higher = 0;
			int lower =  0;
			int same = 0;
			for (AnalysisProcessModel model : analyzedModels.values())
				for (AnalysisModelRevision revision : model.getRevisions().values()) {
					double actualValue = revision.get(metric);
					if (actualValue < 0) lower++;
					else if (actualValue == new Double(0)) same++;
					else higher++;
				}
			features.put(metric.name() + AnalysisConstant.HIGHER.getString(), higher);
			features.put(metric.name() + AnalysisConstant.SAME.getString(), same);
			features.put(metric.name() + AnalysisConstant.LOWER.getString(), lower);
		}
		
		// number of revisions that don't alter the number of any metric
		int alteringRevisions = 0;
		int numberOfRevisions = 0;
		for (AnalysisProcessModel model : analyzedModels.values()) {
			numberOfRevisions += model.getRevisions().size();
			for (AnalysisModelRevision revision : model.getRevisions().values())
				for (METRICS metric : getProcessModelMetrics()) 
					if (!revision.get(metric).equals(new Double(0))) {
						alteringRevisions++;
						break;
					}
		}
					
		features.put(AnalysisConstant.NUM_REVISIONS.getString(), numberOfRevisions);
		features.put(AnalysisConstant.ALTERING_REVISIONS.getString(), alteringRevisions);
		features.put(AnalysisConstant.UNALTERING_REVISIONS.getString(), numberOfRevisions - alteringRevisions);
		
		return features;
	}
}
