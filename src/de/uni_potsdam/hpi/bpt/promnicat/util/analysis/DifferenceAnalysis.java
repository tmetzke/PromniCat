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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;

/**
 * @author Tobias Metzke
 * 
 */
public class DifferenceAnalysis extends MetricsAnalysis {

	protected Collection<METRICS> metrics;

	public DifferenceAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze, Collection<METRICS> metrics) {
		super(modelsToAnalyze);
		this.metrics = metrics;
	}

	@Override
	public void performAnalysis() {

		for (AnalysisProcessModel model : modelsToAnalyze.values()) {
			AnalysisProcessModel newModel = new AnalysisProcessModel(
					model.getName());
			Map<METRICS, Double> oldValues = getInitialMetricsValues();
			// perform the analysis of differences for every revision and metric
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				AnalysisModelRevision newRevision = new AnalysisModelRevision(
						revision.getRevisionNumber());
				for (METRICS metric : metrics) {
					double actualValue = revision.get(metric);
					double oldValue = oldValues.get(metric);
					double difference = calculateDifference(metric,
							actualValue, oldValue);
					// save the new value as back-reference for the next
					// revision
					oldValues.put(metric, actualValue);
					newRevision.add(metric, difference);
					// if a metric is actually lower than in the previous
					// revision,
					// the model is not growing continuously
					if (difference < 0)
						newModel.setGrowing(false);
				}
				newModel.add(newRevision);
			}
			analyzedModels.put(model.getName(), newModel);
		}

	}
	
	/**
	 * initialize a first collection of metrics zero-values to have a starting
	 * point for the first revision of a model to be compared to
	 * @return
	 */
	private Map<METRICS, Double> getInitialMetricsValues() {
		Map<METRICS, Double> oldValues = new HashMap<>();
		for (METRICS metric : metrics)
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
	protected double calculateDifference(METRICS metric, double actualValue, double oldValue) {
		return actualValue - oldValue;
	}
}
