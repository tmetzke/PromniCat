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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.abstractAnalyses.metricAnalyses;

import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisConstant;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisModelRevision;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;

/**
 * @author Tobias Metzke
 *
 */
public class ModelLanguageAnalysis extends AbstractMetricsAnalysis {

	public ModelLanguageAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze) {
		super(modelsToAnalyze);
	}

	@Override
	protected void performAnalysis() {
		for (AnalysisProcessModel model : modelsToAnalyze.values()) {
			AnalysisProcessModel newModel = new AnalysisProcessModel(model.getName());
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				AnalysisModelRevision newRevision = new AnalysisModelRevision(revision.getRevisionNumber());
				if (revision.get(METRICS.NUM_NODES) > 0 || revision.get(METRICS.NUM_EDGES) > 0)
					newRevision.add(AnalysisConstant.CONTROL_FLOW.getDescription(), 1);
				if (revision.get(METRICS.NUM_DATA_NODES) > 0)
					newRevision.add(AnalysisConstant.DATA_FLOW.getDescription(), 1);
				if (revision.get(METRICS.NUM_ROLES) > 0)
					newRevision.add(AnalysisConstant.ORGANISATION.getDescription(), 1);
				newModel.add(newRevision);
			}
			analyzedModels.put(model.getName(), newModel);
		}
	}

	@Override
	protected String addCSVHeader() {
		StringBuilder resultBuilder = new StringBuilder()
			.append("Process Model" + CSV_ITEMSEPARATOR)
			.append("Revision" + CSV_ITEMSEPARATOR)
			.append("Model Language");
		return resultBuilder.toString();
	}

	@Override
	protected String toCsvString(AnalysisProcessModel model) {
		StringBuilder resultBuilder = new StringBuilder();
		// language elements for every revision
		for (AnalysisModelRevision revision : model.getRevisions().values()) {
			resultBuilder
				.append("\n")
				.append(model.getName())
				.append(CSV_ITEMSEPARATOR + revision.getRevisionNumber());
			int count = 0;
			for (String languageElement : revision.getMetrics().keySet()) {
				if (count > 0)
					resultBuilder.append(",");
				else
					resultBuilder.append(CSV_ITEMSEPARATOR);
				resultBuilder.append(languageElement);
				count++;
			}
		}
		return resultBuilder.toString();
	}
}
