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

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisConstants;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.AnalysisHelper;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.ProcessEvolutionModel;
import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class HighLowSameAnalysis extends AbstractAnalysis {

	private Collection<METRICS> metrics;
	private Map<String, Integer> results = new HashMap<String, Integer>();
	private final String higher = AnalysisConstants.HIGHER.getDescription();
	private final String same = AnalysisConstants.SAME.getDescription();
	private final String lower = AnalysisConstants.LOWER.getDescription();
	private final String[] measures = {higher, same, lower};
	
	/**
	 * @param modelsToAnalyze
	 */
	public HighLowSameAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze, Map<String, ProcessEvolutionModel> analyzedModels, Collection<METRICS> metrics) {
		super(modelsToAnalyze, analyzedModels);
		this.metrics = metrics;
	}
	
	public HighLowSameAnalysis(Map<String, ProcessEvolutionModel> modelsToAnalyze, Collection<METRICS> metrics) {
		this(modelsToAnalyze, null, metrics);
	}

	@Override
	protected void performAnalysis() {
		IAnalysis differenceAnalysis = AnalysisHelper.analyzeDifferencesInMetrics(modelsToAnalyze, false);
		Map<String, ProcessEvolutionModel> differenceAnalyzedModels = differenceAnalysis.getAnalyzedModels();
		for (METRICS metric : metrics) {
			int higherValues = 0;
			int lowerValues =  0;
			int sameValues = 0;
			for (ProcessEvolutionModel model : differenceAnalyzedModels.values())
				for (ProcessEvolutionModelRevision revision : model.getRevisions().values()) {
					double actualValue = revision.get(metric);
					if (actualValue < 0) lowerValues++;
					else if (actualValue == new Double(0)) sameValues++;
					else higherValues++;
				}
			results.put(metric.name() + higher, higherValues);
			results.put(metric.name() + same, sameValues);
			results.put(metric.name() + lower, lowerValues);
		}
	}

	@Override
	protected String getResultCSVString() {
		StringBuilder builder = new StringBuilder();
		for (METRICS metric : metrics)
			builder.append(CSV_ITEMSEPARATOR + metric);
		for (String measure : measures) {
			builder
				.append("\n")
				.append(measure);
			for (METRICS metric : metrics)
				builder.append(CSV_ITEMSEPARATOR + results.get(metric.name() + measure));
		}
		return builder.toString();
	}

}
